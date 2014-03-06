package com.gunit.spacecrack.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.chat.Chat;
import com.gunit.spacecrack.chat.ChatActivity;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dimitri on 4/03/14.
 */
public class SpaceCrackService extends Service {

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private int chatNotification;
    private Firebase firebase;
    private String username;
    private String gameId;
    private final IBinder binder = new LocalBinder();
    private Map<String, ValueEventListener> valueEventListeners;
    private SharedPreferences sharedPreferences;
    private boolean notifications;
    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        valueEventListeners = new HashMap<String, ValueEventListener>();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        gson = new Gson();
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(chatNotification);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        username = intent.getStringExtra("username");
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void addFirebaseListener(String url, String gameId) {
        if (valueEventListeners.get(gameId) == null) {
            this.gameId = gameId;
            firebase = new Firebase(url + "/" + gameId);
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    notifications = sharedPreferences.getBoolean("pref_notifications", true);
                    if (!isForeground("com.gunit.spacecrack") && notifications) {
                        showNotification();
                    }
                }

                @Override
                public void onCancelled() {

                }
            };
            valueEventListeners.put(gameId, valueEventListener);
            firebase.addValueEventListener(valueEventListener);
        }
    }

    public void removeFirebaseListener(String gameId) {
        firebase.removeEventListener(valueEventListeners.get(gameId));
    }

    private void showNotification() {
        chatNotification = R.string.new_message;
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getText(chatNotification))
                .setAutoCancel(true);

        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("gameId", gameId);
        chatIntent.putExtra("username", username);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ChatActivity.class);
        stackBuilder.addNextIntent(chatIntent);

        PendingIntent chatPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(chatPendingIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(chatNotification, builder.build());
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

    public class LocalBinder extends Binder {
        public SpaceCrackService getService() {
            return SpaceCrackService.this;
        }
    }
}
