package com.waitservicepay;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.waitservicepay.databinding.ActivityMainBinding;
import com.waitservicepay.databinding.ActivitySettingsBinding;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity{
    enum Action{
        RUN_SHUTDOWN,
        OPEN_SETTINGS,
    }

    final int defaultExitTime = 30;
    final int minTime = 10;
    final int maxTime = 100;
    final String STORAGE = "StorageSettings.xml";

    private int timeExit;

    private ActivityMainBinding binding;
    private ActivitySettingsBinding bindingSetting;


    private SharedPreferences settings = null;
    private SharedPreferences.Editor editor = null;

    //Main program stop timer

    Timer mainTimer;
    //Touch screen hold timer
    Timer holdTimer;


    boolean isExit;
    boolean isBlockedMotionEvent;
    boolean isBlockExitProgramEnable;

    void initialization(){
        isExit = false;
        isBlockedMotionEvent = false;
        isBlockExitProgramEnable = false;

        initSettings();
        loadParam();
    }

    private void initSettings(){
        settings = getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    private void loadParam(){
        if(null == settings){
            initSettings();
        }

        isBlockExitProgramEnable = settings.getBoolean("isBlockExitProgramEnable", false);
        timeExit = settings.getInt("timeExit", defaultExitTime);
    }

    private void saveParam(){
        if(null == settings){
            initSettings();
        }

        editor.putBoolean("isBlockExitProgramEnable", isBlockExitProgramEnable);
        editor.putInt("timeExit", timeExit);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initialization();

        activitySettings();
        activityMain();

        mainTimer = startTimer(timeExit, Action.RUN_SHUTDOWN);
    }
    private void activityMain(){
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.imageView.animate().alpha(1f).setDuration(750);
        binding.textView.setText("Запуск");
        binding.textView.animate().alpha(1f).setDuration(750);

        setContentView(binding.getRoot());
    }

    private void activitySettings(){
        bindingSetting = ActivitySettingsBinding.inflate(getLayoutInflater());

        bindingSetting.buttonBack.setText("Применить");
        bindingSetting.textHeader.setText("\nНастройки\n");
        bindingSetting.textAbout.setText("Smith App\n" +
                "WaitServicePay\n\n" +
                "Version 1.1.3\n");
        bindingSetting.switchBlockProgramDisable.setText("Закрыть\nпосле события ");
        bindingSetting.switchBlockProgramDisable.setChecked(!isBlockExitProgramEnable);
        bindingSetting.textViewTimer.setText("Таймер: ");
        bindingSetting.editTextNumber.setHint(String.valueOf(timeExit));

        bindingSetting.editTextNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!v.getText().toString().isEmpty()) {
                    int value = Integer.parseInt(v.getText().toString());
                    if(minTime > value){
                        v.setText(Integer.toString(minTime));
                    }
                    else if(maxTime < value){
                        v.setText(Integer.toString(maxTime));
                    }
                    bindingSetting.editTextNumber.setHint(v.getText());
                    timeExit = Integer.parseInt(v.getText().toString());
                }

                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                return true;
            }
        });

        bindingSetting.textViewTimerInfo.setText("Мин " + minTime + ", Макс " + maxTime);

        bindingSetting.buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                clickedBack();
            }
        });
    }

    //Function for exiting activity settings
    void clickedBack(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        isBlockedMotionEvent = false;
        binding.textView.setText("Запуск");

        isBlockExitProgramEnable = !bindingSetting.switchBlockProgramDisable.isChecked();
        bindingSetting.editTextNumber.setText("");

        saveParam();

        mainTimer = startTimer(timeExit, Action.RUN_SHUTDOWN);

        setContentView(binding.getRoot());
    }

    @NonNull
    private Timer startTimer(int stopTime, Action task){
        long delay = 1000;
        long period = 1000;

        return  startTimer(stopTime, task, delay, period);
    }

    //Start a timer for different task
    @NonNull
    private Timer startTimer(final int stopTime, final Action task, final long delay, final long period){
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            private int time = stopTime;
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(time < 1){
                            switch (task) {
                                case RUN_SHUTDOWN:
                                    finishAndRemoveTask();
                                    break;
                                case OPEN_SETTINGS:
                                    isBlockedMotionEvent = true;
                                    isBlockExitProgramEnable = true;
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                    setContentView(bindingSetting.getRoot());
                                    break;
                            }
                            cancel();
                        }
                        else {
                            switch (task){
                                case RUN_SHUTDOWN:
                                    binding.textView.setText("" + time);

                                    break;
                                case OPEN_SETTINGS:
                                    binding.textView.setText("Настройки");
                                    break;
                            }

                            isExit = false;
                            time--;
                        }
                    }
                });
            }
        }, delay, period);
        return timer;
    }

    private void stopAllTimer(){
        if (mainTimer != null) {
            mainTimer.cancel();
        }
        if (holdTimer != null) {
            holdTimer.cancel();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isBlockedMotionEvent) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    restartTimer();
                    break;
                case MotionEvent.ACTION_DOWN:
                    stopAllTimer();
                    holdTimer = startTimer(1, Action.OPEN_SETTINGS);
                    binding.textView.setText("");
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    //Reset or exit the program
    private void restartTimer(){
        stopAllTimer();
        if (isExit) {
            finishAndRemoveTask();
        } else {
            mainTimer = startTimer(timeExit, Action.RUN_SHUTDOWN);
            binding.textView.setText("Закрыть");
            isExit = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(isBlockedMotionEvent) {
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                clickedBack();
                return true;
            }
        }
        else{
            restartTimer();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(!isBlockExitProgramEnable) {
            finishAndRemoveTask();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!isBlockedMotionEvent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if(!isBlockExitProgramEnable) {
            finishAndRemoveTask();
        }
    }
}
