package com.gunit.spacecrack.activity;

import android.test.ActivityInstrumentationTestCase2;

import com.gunit.spacecrack.activity.ProfileActivity;
import com.robotium.solo.Solo;

/**
 * Created by Dimitri on 20/02/14.
 */
public class ProfileTest extends ActivityInstrumentationTestCase2<ProfileActivity> {

    private Solo solo;
    private ProfileActivity activity;

    public ProfileTest() {
        super(ProfileActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        solo = new Solo(getInstrumentation(), activity);
    }

    public void testActivityStart() throws Exception {
        solo.assertCurrentActivity("Current activity should be ProfileActivity", ProfileActivity.class);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }
}
