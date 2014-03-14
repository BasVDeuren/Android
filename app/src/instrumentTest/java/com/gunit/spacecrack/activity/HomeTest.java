package com.gunit.spacecrack.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.test.ActivityInstrumentationTestCase2;

import com.gunit.spacecrack.R;
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
        solo.waitForFragmentByTag("Home");
    }

    public void testActivityStart() throws Exception {
        solo.assertCurrentActivity("Current activity should be HomeActivity", HomeActivity.class);
        Fragment homeFragment = ((FragmentActivity) solo.getCurrentActivity()).getSupportFragmentManager().findFragmentByTag("Home");
        assertTrue("HomeFragment should be visible", homeFragment.isVisible());
    }

    public void testProfile() throws Exception {
        solo.clickOnView(solo.getView(R.id.llt_home_profile));
        solo.waitForActivity(ProfileActivity.class);
        solo.assertCurrentActivity("Current activity should be ProfileActivity", ProfileActivity.class);
    }

    public void testHelp() throws Exception {
        solo.clickOnView(solo.getView(R.id.btn_home_help));
        solo.waitForFragmentByTag("Help");
        Fragment settingsFragment = ((FragmentActivity) solo.getCurrentActivity()).getSupportFragmentManager().findFragmentByTag("Help");
        assertTrue("HelpFragment should be visible", settingsFragment.isVisible());
    }

    public void testSettings() throws Exception {
        solo.clickOnView(solo.getView(R.id.btn_home_settings));
        solo.waitForActivity(SettingsActivity.class);
        solo.assertCurrentActivity("Current activity should be SettingsActivity", SettingsActivity.class);
    }

    public void testLogout() throws Exception {
        solo.clickOnView(solo.getView(R.id.btn_home_logout));
        solo.waitForActivity(LoginActivity.class);
        solo.assertCurrentActivity("Current activity should be LoginActivity", LoginActivity.class);
    }

    public void testNewGame() throws Exception {
        solo.clickOnView(solo.getView(R.id.btn_home_newgame));
        solo.waitForFragmentByTag("NewGame");
        Fragment newGameFragment = ((FragmentActivity) solo.getCurrentActivity()).getSupportFragmentManager().findFragmentByTag("NewGame");
        assertTrue("NewGameFragment should be visible", newGameFragment.isVisible());
    }

    public void testLobby() throws Exception {
        solo.clickOnButton(solo.getString(R.string.lobby));
        solo.waitForFragmentByTag("Lobby");
        Fragment lobbyFragment = ((FragmentActivity) solo.getCurrentActivity()).getSupportFragmentManager().findFragmentByTag("Lobby");
        assertTrue("LobbyFragment should be visible", lobbyFragment.isVisible());
    }

    public void testReplay() throws Exception {
        solo.clickOnButton(solo.getString(R.string.replays));
        solo.waitForFragmentByTag("Replay");
        Fragment replayFragment = ((FragmentActivity) solo.getCurrentActivity()).getSupportFragmentManager().findFragmentByTag("Replay");
        assertTrue("ReplayFragment should be visible", replayFragment.isVisible());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }
}
