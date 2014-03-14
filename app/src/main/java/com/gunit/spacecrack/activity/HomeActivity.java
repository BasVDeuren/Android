package com.gunit.spacecrack.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Space;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.fragment.HomeFragment;
import com.gunit.spacecrack.fragment.LobbyFragment;
import com.gunit.spacecrack.service.SpaceCrackService;

/**
 * HomeActivity used for navigation to other activities and fragments
 */
public class HomeActivity extends FragmentActivity {

    private SpaceCrackService spaceCrackService;
    private boolean boundToService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("EXIT", false)) {
            System.exit(0);
            finish();
        } else if (intent.getBooleanExtra("invite", false)) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new LobbyFragment(), "Lobby")
                    .commit();
        } else {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new HomeFragment(), "Home")
                        .commit();
            }
        }

        //Bind to the service
        Intent service = new Intent(this, SpaceCrackService.class);
        service.putExtra("username", SpaceCrackApplication.user.username);
        startService(service);
        bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (boundToService) {
            unbindService(serviceConnection);
            boundToService = false;
        }
        super.onStop();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpaceCrackService.LocalBinder binder = (SpaceCrackService.LocalBinder) service;
            spaceCrackService = binder.getService();
            String username = SpaceCrackApplication.user.username.replaceAll("\\s","");
            spaceCrackService.addInviteListener(SpaceCrackApplication.URL_FIREBASE_INVITES + "/" + username + SpaceCrackApplication.user.profile.profileId);
            boundToService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundToService = false;
        }
    };
}
