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
public class ProfileTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private Solo solo;

    public ProfileTest() {
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
    }

    public void testActivityStart() throws Exception {
        solo.assertCurrentActivity("Current activity should be ProfileActivity", ProfileActivity.class);
        solo.waitForFragmentByTag("Profile");
        Fragment profileFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Profile");
        assertTrue("ProfileFragment should be visible", profileFragment.isVisible());
    }

    public void testEditProfile() throws Exception {
        solo.clickOnButton(0);
        solo.waitForFragmentByTag("Edit Profile");
        Fragment editProfileFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Edit Profile");
        assertTrue("EditProfileFragment should be visible", editProfileFragment.isVisible());
    }

    public void testPassword() throws Exception {
        solo.clickOnButton(1);
        solo.waitForFragmentByTag("Password");
        Fragment passwordFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Password");
        assertTrue("PasswordFragment should be visible", passwordFragment.isVisible());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }
}
