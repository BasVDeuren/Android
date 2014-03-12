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
import android.os.Binder;
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
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.chat.Chat;
import com.gunit.spacecrack.chat.ChatActivity;
import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.json.MapWrapper;
import com.gunit.spacecrack.model.Game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dimitri on 4/03/14.
 */
public class SpaceCrackService extends Service {

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private int notificationId;
    private Firebase firebase;
    private String username;
    private String gameId;
    private final IBinder binder = new LocalBinder();
    private Map<Integer, ValueEventListener> valueEventListeners;
    private Map<String, ChildEventListener> childEventListeners;
    private SharedPreferences sharedPreferences;
    private boolean notifications;
    private int activePLayer;
    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Service", "Create");
        valueEventListeners = new HashMap<Integer, ValueEventListener>();
        childEventListeners = new HashMap<String, ChildEventListener>();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        gson = new Gson();

//        Runnable helloRunnable = new Runnable() {
//            public void run() {
//                Log.i("Service", "Hello world");
//            }
//        };
//
//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
//        executor.scheduleAtFixedRate(helloRunnable, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Service", "StartCommand");
        if (intent != null) {
            username = intent.getStringExtra("username");
//            addGameListener(SpaceCrackApplication.URL_FIREBASE_CHAT + "/1", 1, true);
        }
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Service", "Bind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("Service", "Unbind");
        return super.onUnbind(intent);
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

    public void addGameListener(String url, final int playerId, final boolean first) {
        if (valueEventListeners.get(playerId) == null) {
            firebase = new Firebase(url);
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        notifications = sharedPreferences.getBoolean("pref_notifications", true);
                        if (!isForeground("com.gunit.spacecrack") && notifications) {
                            Game game = gson.fromJson(dataSnapshot.getValue().toString(), Game.class);
                            if (first) {
                                if (game.player2.turnEnded) {
                                    showNotification(game.name, getText(R.string.your_turn).toString(), false);
                                }
                            } else {
                                if (game.player1.turnEnded) {
                                    showNotification(game.name, getText(R.string.your_turn).toString(), false);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled() {

                }
            };
            valueEventListeners.put(playerId, valueEventListener);
            firebase.addValueEventListener(valueEventListener);
        }
    }

    public void addChatListener(String url, String gameId) {
        if (childEventListeners.get(gameId) == null) {
            this.gameId = gameId;
            firebase = new Firebase(url + "/" + gameId);
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        notifications = sharedPreferences.getBoolean("pref_notifications", true);
                        Log.i("Chat", dataSnapshot.getValue().toString());
                        Chat chat = gson.fromJson(dataSnapshot.getValue().toString(), Chat.class);
                        Log.i("Chat object", chat.getFrom());
                        if (!isForeground("com.gunit.spacecrack") && notifications) {
                            showNotification(chat.getFrom(), chat.getBody(), true);
                        }
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
            childEventListeners.put(gameId, childEventListener);
            firebase.addChildEventListener(childEventListener);
        }
    }

    public void removeFirebaseListener(String gameId) {
//        firebase.removeEventListener(valueEventListeners.get(gameId));
        firebase.removeEventListener(childEventListeners.get(gameId));
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

    private boolean isForeground(String PackageName) {
        // Get the Activity Manager
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        // Get a list of running tasks, we are only interested in the last one,
        // the top most so we give a 1 as parameter so we only get the topmost.
        List<ActivityManager.RunningTaskInfo> task = manager.getRunningTasks(1);

        // Get the info we need for comparison.
        ComponentName componentInfo = task.get(0).topActivity;

        // Check if it matches our package name.
        if (componentInfo.getPackageName().equals(PackageName)) return true;

        // If not then our app is not on the foreground.
        return false;
    }

    public class LocalBinder extends Binder {
        public SpaceCrackService getService() {
            return SpaceCrackService.this;
        }
    }

    private void saveListeners() {
        Log.i("Service", "Save listeners");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        MapWrapper childEventListenerWrapper = new MapWrapper();
        childEventListenerWrapper.childEventListenerHashMap = childEventListeners;
        String childWrapper = gson.toJson(childEventListenerWrapper);
        editor.putString("childWrapper", childWrapper);
    }
}
