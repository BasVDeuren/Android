package com.gunit.spacecrack.activity;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageView;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.activity.HomeActivity;
import com.gunit.spacecrack.activity.ProfileActivity;
import com.robotium.solo.Solo;

/**
 * Created by Dimitri on 20/02/14.
 */
public class HomeTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private Solo solo;
    private LoginActivity loginActivity;

    public HomeTest() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        loginActivity = getActivity();
        solo = new Solo(getInstrumentation(), loginActivity);
        solo.enterText(0, "test@gmail.com");
        solo.enterText(1, "test");
        solo.clickOnButton(solo.getString(R.string.login));
        solo.waitForActivity(HomeActivity.class);
    }

    public void testActivityStart() throws Exception {
        solo.assertCurrentActivity("Current activity should be HomeActivity", HomeActivity.class);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }
}
