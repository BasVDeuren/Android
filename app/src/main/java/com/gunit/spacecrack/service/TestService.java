package com.gunit.spacecrack.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dimitri on 6/03/14.
 */
public class TestService extends Service {

    private Timer timer = new Timer();
    private static long UPDATE_INTERVAL = 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "Create");
        Runnable helloRunnable = new Runnable() {
            public void run() {
                Log.i("Service", "Hello test");
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("Service", "StartCommand");
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("Service", "Unbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("Service", "Destroy");
        super.onDestroy();
    }
}
