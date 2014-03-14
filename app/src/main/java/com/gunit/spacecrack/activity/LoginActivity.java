package com.gunit.spacecrack.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.Session;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.fragment.LoginFragment;

/**
 * LoginActivity used for Login and Register
 */
public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new LoginFragment(), "Login")
                    .commit();
        }
    }

    /**
     * Handle the result from the Facebook Login Dialog
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

}
