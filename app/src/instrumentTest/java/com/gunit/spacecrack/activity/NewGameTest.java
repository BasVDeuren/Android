package com.gunit.spacecrack.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.test.ActivityInstrumentationTestCase2;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.fragment.NewGameFragment;
import com.robotium.solo.Solo;

/**
 * Created by Dimitri on 20/02/14.
 */
public class NewGameTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private Solo solo;
    private NewGameFragment newGameFragment;

    public NewGameTest() {
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
        solo.clickOnView(solo.getView(R.id.btn_home_newgame));
        solo.waitForFragmentByTag("NewGame");
        newGameFragment = (NewGameFragment) ((FragmentActivity) solo.getCurrentActivity()).getSupportFragmentManager().findFragmentByTag("NewGame");
    }

    public void testRandom() throws Exception {
        solo.clickOnView(solo.getView(R.id.btn_newgame_random));
        solo.waitForLogMessage("Random user selected");
        assertNotNull("A random player should be selected", newGameFragment.getSelectedUser());
    }

    public void testSearchUsers() throws Exception {
        solo.clearEditText(0);
        solo.clearEditText(1);
        solo.enterText(0, "RobotiumGame");
        solo.enterText(1, "Opponent");
        solo.clickOnView(solo.getView(R.id.btn_newgame_search));
        solo.waitForLogMessage("Users retrieved");
        assertTrue("At least one user should be found", newGameFragment.getUsers().size() > 0);
        assertTrue("HelpFragment should be visible", newGameFragment.isVisible());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

}
