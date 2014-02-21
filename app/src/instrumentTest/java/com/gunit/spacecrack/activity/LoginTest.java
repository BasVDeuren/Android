package com.gunit.spacecrack.activity;

import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.activity.LoginActivity;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.robotium.solo.Solo;

/**
 * Created by Dimi on 4/02/14.
 */
public class LoginTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private Solo solo;
    private LoginActivity activity;

    public LoginTest() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        solo = new Solo(getInstrumentation(), activity);
    }

    public void testActivityStart() throws Exception {
        solo.assertCurrentActivity("Current activity should be LoginActivity", LoginActivity.class);
    }

    public void testLoginSucces() throws Exception {
        solo.enterText(0, "test");
        solo.enterText(1, "test");
        solo.clickOnButton(solo.getString(R.string.login));
        solo.waitForLogMessage("Logged in");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Login", 0);
        String accessToken = sharedPreferences.getString("accessToken", null);
        assertNotNull("Token should not be null", accessToken);
    }

    public void testLoginFailed() throws Exception {
        solo.enterText(0, "test");
        solo.enterText(1, "pass");
        solo.clickOnButton(solo.getString(R.string.login));

        solo.waitForLogMessage("Login failed");
        SpaceCrackApplication application = (SpaceCrackApplication) getActivity().getApplication();
        assertNull("Token should be null", SpaceCrackApplication.accessToken);
    }

//    public void testRegister() throws Exception {
//        solo.clickOnButton(getActivity().getResources().getString(R.string.request));
//        solo.waitForActivity(RegisterActivity.class);
//        solo.assertCurrentActivity("Current activity should be RegisterActivity", RegisterActivity.class);
//    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }
}