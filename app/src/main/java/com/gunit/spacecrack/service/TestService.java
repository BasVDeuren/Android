package com.gunit.spacecrack.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.activity.SplashScreenActivity;
import com.gunit.spacecrack.chat.Chat;
import com.gunit.spacecrack.json.MapWrapper;
import com.gunit.spacecrack.model.Game;

import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dimitri on 6/03/14.
 */
public class TestService extends Service {

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private int notificationId;
    private String username;
    private String gameId;
    private Map<Integer, ValueEventListener> valueEventListeners;
    private Map<String, ChildEventListener> childEventListeners;
    private SharedPreferences sharedPreferences;
    private boolean notifications;
    private int activePLayer;
    private Gson gson;
    private Timer timer = new Timer();
    private static long UPDATE_INTERVAL = 1000;
    private Firebase firebase;
    private ChildEventListener childEventListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "Create");
        gson = new Gson();
        gameId = "1";
        firebase = null;

        childEventListeners = new HashMap<String, ChildEventListener>();
//        sharedPreferences = getApplicationContext().getSharedPreferences("com.gunit.spacecrack", Context.MODE_PRIVATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String childWrapper = sharedPreferences.getString("childWrapper", "");
        Log.i("childWrapper", childWrapper);
        if (!childWrapper.equals("")) {
            Log.i("Service", "Childwrapper is filled in");
            MapWrapper wrapper = gson.fromJson(childWrapper, MapWrapper.class);
            childEventListeners = wrapper.childEventListenerHashMap;
            for (Map.Entry<String, ChildEventListener> entry : childEventListeners.entrySet()) {
                firebase = new Firebase("https://amber-fire-3394.firebaseio.com/" + entry.getKey());
                firebase.addChildEventListener(entry.getValue());
            }
        } else {
            firebase = new Firebase("https://amber-fire-3394.firebaseio.com/1");
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
//                        Log.i("ChatTest", dataSnapshot.getValue().toString());
                        Chat chat = gson.fromJson(dataSnapshot.getValue().toString(), Chat.class);
//                        Log.i("Chat Test object", chat.getFrom());
                        showNotification(chat.getFrom(), chat.getBody(), true);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled() {

                }
            };
            firebase.addChildEventListener(childEventListener);
            childEventListeners.put(gameId, childEventListener);
        }

//        Runnable helloRunnable = new Runnable() {
//            int counter = 0;
//            public void run() {
//                Log.i("Service", "Hello world" + counter++);
//            }
//        };
//
//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
//        executor.scheduleAtFixedRate(helloRunnable, 0, 5, TimeUnit.SECONDS);

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
    public boolean stopService(Intent name) {
        Log.d("Service", "Stop service");
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        Log.d("Service", "Destroy");
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Log.i("Service", "Task Removed");
        saveListeners();
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    private void saveListeners() {
        Log.i("Service", "Save listeners");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        MapWrapper childEventListenerWrapper = new MapWrapper();
        childEventListenerWrapper.childEventListenerHashMap = childEventListeners;
        String childWrapper = gson.toJson(childEventListenerWrapper);
        editor.putString("childWrapper", childWrapper);
    }

    private void showNotification(String title, String text, boolean chat) {
        notificationId = R.string.new_message;
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        if (chat) {
            Intent chatIntent = new Intent(this, SplashScreenActivity.class);
            chatIntent.putExtra("task", "chat");
            chatIntent.putExtra("gameId", gameId);
            chatIntent.putExtra("username", username);
            stackBuilder.addParentStack(SplashScreenActivity.class);
            stackBuilder.addNextIntent(chatIntent);
        } else {
            Intent gameIntent = new Intent(this, SplashScreenActivity.class);
            gameIntent.putExtra("task", "game");
            gameIntent.putExtra("gameId", Integer.valueOf(gameId));
            stackBuilder.addParentStack(SplashScreenActivity.class);
            stackBuilder.addNextIntent(gameIntent);
        }

        PendingIntent chatPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(chatPendingIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }

    private boolean isForeground(String PackageName){
        // Get the Activity Manager
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        // Get a list of running tasks, we are only interested in the last one,
        // the top most so we give a 1 as parameter so we only get the topmost.
        List< ActivityManager.RunningTaskInfo > task = manager.getRunningTasks(1);

        // Get the info we need for comparison.
        ComponentName componentInfo = task.get(0).topActivity;

        // Check if it matches our package name.
        if(componentInfo.getPackageName().equals(PackageName)) return true;

        // If not then our app is not on the foreground.
        return false;
    }
}
