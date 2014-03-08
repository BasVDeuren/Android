package com.gunit.spacecrack.activity;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.activity.LoginActivity;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.fragment.RegisterFragment;
import com.robotium.solo.Solo;

/**
 * Created by Dimi on 4/02/14.
 */
public class LoginTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private Solo solo;

    public LoginTest() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testActivityStart() throws Exception {
        solo.assertCurrentActivity("Current activity should be LoginActivity", LoginActivity.class);
        Fragment loginFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Login");
        assertTrue("LoginFragment should be visible", loginFragment.isVisible());
    }

    public void testLoginSucces() throws Exception {
        solo.enterText(0, "test@gmail.com");
        solo.enterText(1, "test");
        solo.clickOnButton(solo.getString(R.string.login));
        solo.waitForLogMessage("Login success");
        assertNotNull("Token should not be null", SpaceCrackApplication.accessToken);
    }

    public void testLoginFailed() throws Exception {
        solo.enterText(0, "test@gmail.com");
        solo.enterText(1, "pass");
        solo.clickOnButton(solo.getString(R.string.login));
        solo.waitForLogMessage("Login failed");
        assertNull("Token should be null", SpaceCrackApplication.accessToken);
    }

    public void testRegister() throws Exception {
        solo.clickOnButton(getActivity().getResources().getString(R.string.register));
        solo.waitForFragmentByTag("Register");
        Fragment registerFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Register");
        assertTrue("RegisterFragment should be visible", registerFragment.isVisible());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }
}