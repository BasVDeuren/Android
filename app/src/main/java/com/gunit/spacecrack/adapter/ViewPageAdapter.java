package com.gunit.spacecrack.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by Dimitri on 12/03/14.
 */

/**
 * Adapter used to display the desired Fragment in the LobbyFragment
 */
public class ViewPageAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments;

    public ViewPageAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
        super(fragmentManager);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
