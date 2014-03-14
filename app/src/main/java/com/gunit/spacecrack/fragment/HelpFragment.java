package com.gunit.spacecrack.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.json.GameViewModel;

import java.util.ArrayList;

/**
 * Created by Dimitri on 13/03/14.
 */

/**
 * Fragment to show the Game objectives
 */
public class HelpFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        return view;
    }
}
