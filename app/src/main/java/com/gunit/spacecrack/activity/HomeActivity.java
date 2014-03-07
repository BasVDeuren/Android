package com.gunit.spacecrack.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.fragment.HomeFragment;
import com.gunit.spacecrack.fragment.LoginFragment;
import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.service.SpaceCrackService;
import com.gunit.spacecrack.service.TestService;

import java.util.List;

public class HomeActivity extends Activity {

    private final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new HomeFragment(), "Home")
                    .commit();
        }

//        Intent intent = new Intent(this, TestService.class);
//        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
