package com.gunit.spacecrack.activity;

import android.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageView;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.activity.HomeActivity;
import com.gunit.spacecrack.activity.ProfileActivity;
import com.robotium.solo.Solo;

/**
 * Created by Dimitri on 20/02/14.
 */
public class RegisterTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private Solo solo;

    public RegisterTest() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
        solo.clickOnButton(solo.getString(R.string.register));
        solo.waitForFragmentByTag("Register");
    }

    public void testActivityStart() throws Exception {
        Fragment registerFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Register");
        assertTrue("RegisterFragment should be visible", registerFragment.isVisible());
    }

    public void testRegister() throws Exception {
        solo.enterText(0, "Robotium");
        solo.enterText(1, "robotium");
        solo.enterText(2, "robotium");
        solo.enterText(3, "robotium@gmail.com");
        solo.clickOnButton(solo.getString(R.string.register));
        solo.waitForActivity(HomeActivity.class);
        solo.assertCurrentActivity("Current activity should be HomeActivity", HomeActivity.class);
    }

    public void testRegisterFailed() throws Exception {
        solo.clickOnButton(solo.getString(R.string.register));
        solo.assertCurrentActivity("Current activity should be LoginActivity", LoginActivity.class);
    }

    public void testPasswordsUnmatch() throws Exception {
        solo.enterText(0, "Robotium");
        solo.enterText(1, "robotium");
        solo.enterText(2, "robotium2");
        solo.enterText(3, "robotium@gmail.com");
        solo.clickOnButton(solo.getString(R.string.register));
        solo.assertCurrentActivity("Current activity should be LoginActivity", LoginActivity.class);
    }

    public void testEmailFail() throws Exception {
        solo.enterText(0, "Robotium2");
        solo.enterText(1, "robotium");
        solo.enterText(2, "robotium");
        solo.enterText(3, "robotium");
        solo.clickOnButton(solo.getString(R.string.register));
        solo.assertCurrentActivity("Current activity should be LoginActivity", LoginActivity.class);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }
}
