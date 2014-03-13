package com.gunit.spacecrack.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.fragment.HomeFragment;

public class HomeActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getIntent().getBooleanExtra("EXIT", false)) {
            System.exit(0);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new HomeFragment(), "Home")
                    .commit();
        }
    }
}
