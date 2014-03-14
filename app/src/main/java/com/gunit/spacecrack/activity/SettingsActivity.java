package com.gunit.spacecrack.activity;

import android.app.Activity;
import android.os.Bundle;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.fragment.SettingsFragment;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment(), "Settings")
                    .commit();
        }
    }
}
