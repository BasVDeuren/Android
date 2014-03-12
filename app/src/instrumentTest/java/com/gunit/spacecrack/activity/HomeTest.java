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
public class HomeTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private Solo solo;

    public HomeTest() {
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
    }

    public void testActivityStart() throws Exception {
        solo.assertCurrentActivity("Current activity should be HomeActivity", HomeActivity.class);
        solo.waitForFragmentByTag("Home");
        Fragment homeFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Home");
        assertTrue("HomeFragment should be visible", homeFragment.isVisible());
    }

    public void testProfile() throws Exception {
        solo.clickOnView(solo.getView(R.id.llt_home_profile));
        solo.waitForActivity(ProfileActivity.class);
        solo.assertCurrentActivity("Current activity should be ProfileActivity", ProfileActivity.class);
    }

    public void testSettings() throws Exception {
        solo.clickOnView(solo.getView(R.id.btn_home_settings));
        solo.waitForFragmentByTag("Settings");
        Fragment settingsFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Settings");
        assertTrue("SettingsFragment should be visible", settingsFragment.isVisible());
    }

    public void testNewGame() throws Exception {
        solo.clickOnButton(solo.getString(R.string.new_game));
        solo.waitForFragmentByTag("New Game");
        Fragment newGameFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("New Game");
        assertTrue("NewGameFragment should be visible", newGameFragment.isVisible());
    }

    public void testActiveGames() throws Exception {
        solo.clickOnButton(solo.getString(R.string.new_game));
        solo.waitForFragmentByTag("New Game");
        Fragment activeGamesFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Active Games");
        assertTrue("ActiveGamesFragment should be visible", activeGamesFragment.isVisible());
    }

    public void testReplay() throws Exception {
        solo.clickOnButton(solo.getString(R.string.new_game));
        solo.waitForFragmentByTag("New Game");
        Fragment replayFragment = solo.getCurrentActivity().getFragmentManager().findFragmentByTag("Replay");
        assertTrue("ReplayFragment should be visible", replayFragment.isVisible());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }
}
