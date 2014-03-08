package com.gunit.spacecrack.activity;

import android.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;

import com.gunit.spacecrack.R;
import com.robotium.solo.Solo;

/**
 * Created by Dimitri on 7/03/14.
 */
public class PasswordTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private Solo solo;

    public PasswordTest() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
        solo.enterText(0, "test@gmail.com");
        solo.enterText(1, "test");
        solo.clickOnButton(solo.getString(R.string.login));
        solo.waitForActivity(HomeActivity.class);
        solo.clickOnView(solo.getView(R.id.llt_home_profile));
        solo.waitForActivity(ProfileActivity.class);
        solo.clickOnButton(1);
        solo.waitForFragmentByTag("Password");
    }

    public void testActivityStart() throws Exception {
        solo.assertCurrentActivity("Current activity should be ProfileActivity", ProfileActivity.class);
        Fragment passwordFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Password");
        assertTrue("PasswordFragment should be visible", passwordFragment.isVisible());
    }

    public void testPasswords() throws Exception {
        solo.enterText(0, "test");
        solo.enterText(1, "test2");
        solo.enterText(2, "test2");
        solo.clickOnButton(0);
        solo.waitForFragmentByTag("Profile");
        Fragment passwordFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Profile");
        assertTrue("ProfileFragment should be visible", passwordFragment.isVisible());
    }

    @Override
    public void tearDown() throws Exception {
        solo.clickOnButton(1);
        solo.waitForFragmentByTag("Password");
        solo.enterText(0, "test2");
        solo.enterText(1, "test");
        solo.enterText(2, "test");
        solo.clickOnButton(0);
        solo.waitForFragmentByTag("Profile");
        solo.finishOpenedActivities();
        super.tearDown();
    }
}
