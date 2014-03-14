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
import com.gunit.spacecrack.model.Invite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dimitri on 4/03/14.
 */

/**
 * Background Service to display the notifications
 */
public class SpaceCrackService extends Service {

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private int notificationId;
    private Firebase gameFirebase;
    private Firebase chatFirebase;
    private Firebase inviteFirebase;
    private String username;
    private String gameId;
    private final IBinder binder = new LocalBinder();
    private Map<Integer, ValueEventListener> valueEventListeners;
    private Map<String, ChildEventListener> childEventListeners;
    private ChildEventListener inviteEventListener;
    private SharedPreferences sharedPreferences;
    private boolean notifications;
    private int activePLayer;
    private Gson gson;

    private final int GAME_NOTIFICATION = 0;
    private final int CHAT_NOTIFICATION = 1;
    private final int INVITE_NOTIFICATION = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Service", "Create");
        valueEventListeners = new HashMap<Integer, ValueEventListener>();
        childEventListeners = new HashMap<String, ChildEventListener>();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        gson = new Gson();
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
        }
        return Service.START_REDELIVER_INTENT;
    }

    /**
     * Activity will bind with this service for communication
     * @param intent
     * @return
     */
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
            gameFirebase = new Firebase(url);
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        notifications = sharedPreferences.getBoolean("pref_notifications", true);
                        if (notifications) {
                            Game game = gson.fromJson(dataSnapshot.getValue().toString(), Game.class);
                            if (first) {
                                if (game.player2.turnEnded) {
                                    showNotification(game.name, getText(R.string.your_turn).toString(), GAME_NOTIFICATION, null, null);
                                }
                            } else {
                                if (game.player1.turnEnded) {
                                    showNotification(game.name, getText(R.string.your_turn).toString(), GAME_NOTIFICATION, null, null);
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
            gameFirebase.addValueEventListener(valueEventListener);
        }
    }

    public void addChatListener(String url, String gameId) {
        if (childEventListeners.get(gameId) == null) {
            this.gameId = gameId;
            chatFirebase = new Firebase(url + "/" + gameId);
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        notifications = sharedPreferences.getBoolean("pref_notifications", true);
                        Log.i("Chat", dataSnapshot.getValue().toString());
                        Chat chat = gson.fromJson(dataSnapshot.getValue().toString(), Chat.class);
                        Log.i("Chat object", chat.getFrom());
                        if (notifications) {
                            showNotification(chat.getFrom(), chat.getBody(), CHAT_NOTIFICATION, null, null);
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
            chatFirebase.addChildEventListener(childEventListener);
        }
    }

    public void addInviteListener(String url) {
        if (inviteEventListener == null) {
            inviteFirebase = new Firebase(url);
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        notifications = sharedPreferences.getBoolean("pref_notifications", true);
                        Log.i("Datasnapshot", dataSnapshot.getName());
                        Log.i("Invite", dataSnapshot.getValue().toString());
//                        Object invite = dataSnapshot.getValue();
                        Invite invite = gson.fromJson(dataSnapshot.getValue().toString(), Invite.class);

                        if (notifications) {
//                            String readString = String.valueOf(((Map)invite).get("read"));
//                            boolean read = Boolean.valueOf(readString);
//                            if (!read) {
//                                showNotification(getString(R.string.invitation), (String)((Map)invite).get("inviter"), INVITE_NOTIFICATION, invite);
//                            }
                            if (!invite.read) {
                                  showNotification(getString(R.string.invitation), invite.inviter, INVITE_NOTIFICATION, invite, dataSnapshot.getName());
                            }
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
            inviteEventListener = childEventListener;
            inviteFirebase.addChildEventListener(childEventListener);
        }
    }

    private void showNotification(String title, String text, int notificationType, Object object, String name) {
        notificationId = R.string.new_message;
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.spaceship)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        switch (notificationType) {
            case GAME_NOTIFICATION :
                Intent gameIntent = new Intent(this, SplashScreenActivity.class);
                gameIntent.putExtra("task", "game");
                gameIntent.putExtra("gameId", Integer.valueOf(gameId));
                stackBuilder.addParentStack(SplashScreenActivity.class);
                stackBuilder.addNextIntent(gameIntent);
                break;
            case CHAT_NOTIFICATION :
                Intent chatIntent = new Intent(this, SplashScreenActivity.class);
                chatIntent.putExtra("task", "chat");
                chatIntent.putExtra("gameId", gameId);
                chatIntent.putExtra("username", username);
                stackBuilder.addParentStack(SplashScreenActivity.class);
                stackBuilder.addNextIntent(chatIntent);
                break;
            case INVITE_NOTIFICATION :
                if (name != null) {
                    Invite invite = (Invite) object;
                    invite.read = true;
                    Map<String, Object> updates = new HashMap<String, Object>();
                    updates.put("read", true);
                    inviteFirebase.child(name).updateChildren(updates);
                }
                Intent inviteIntent = new Intent(this, SplashScreenActivity.class);
                inviteIntent.putExtra("task", "invite");
                stackBuilder.addParentStack(SplashScreenActivity.class);
                stackBuilder.addNextIntent(inviteIntent);
                break;
        }

        PendingIntent chatPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(chatPendingIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * LocalBinder used for communication with this Service
     */
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
