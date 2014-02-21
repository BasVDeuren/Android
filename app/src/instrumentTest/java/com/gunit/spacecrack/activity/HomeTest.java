package com.gunit.spacecrack.activity;

import android.test.ActivityInstrumentationTestCase2;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.activity.HomeActivity;
import com.gunit.spacecrack.activity.ProfileActivity;
import com.robotium.solo.Solo;

/**
 * Created by Dimitri on 20/02/14.
 */
public class HomeTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    private Solo solo;
    private HomeActivity activity;

    public HomeTest() {
        super(HomeActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        solo = new Solo(getInstrumentation(), activity);
    }

    public void testActivityStart() throws Exception {
        solo.assertCurrentActivity("Current activity should be HomeActivity", HomeActivity.class);
    }

    public void testProfile() throws Exception {
        solo.clickOnView(getActivity().findViewById(R.id.llt_home_profile));
        solo.waitForActivity(ProfileActivity.class);
        solo.assertCurrentActivity("Current activity should be ProfileActivity", ProfileActivity.class);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }
}
