package com.waitmirpay;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.waitmirpay.databinding.ActivityMainBinding;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity{
    private int timeExit = 30;
    private ActivityMainBinding binding;
    Timer mainTimer;
    int time;
    boolean isExit;
    String helloText = "Timer:";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.imageView.animate().alpha(1f).setDuration(750);

        setContentView(binding.getRoot());

        isExit = false;
        startTimer();
    }

    void startTimer(){
        mainTimer = new Timer();
        long delay = 1000;
        long period = 1000;
        time = timeExit;

        mainTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(time < 1){
                            finishAndRemoveTask();
                        }
                        else {
                            binding.textView.setText("" + time);
                            isExit = false;
                            time--;
                        }
                    }
                });
            }
        }, delay, period);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                if(isExit){
                    finishAndRemoveTask();
                }
                else {
                    mainTimer.cancel();
                    startTimer();
                    binding.textView.setText("Закрыть");
                    isExit = true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onPause() {
        super.onPause();
        finishAndRemoveTask();
    }

    @Override
    public void onStop(){
        super.onStop();
        finishAndRemoveTask();
    }
}
