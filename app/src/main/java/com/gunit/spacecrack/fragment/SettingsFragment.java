package com.gunit.spacecrack.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.gunit.spacecrack.R;

/**
 * Created by Dimitri on 4/03/14.
 */

/**
 * SettingsFragment to customize the user's preferences
 */
public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences sharedPreferences;
    private final String notificationsKey = "pref_notifications";

    private CheckBoxPreference notificationPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        notificationPref = (CheckBoxPreference) getPreferenceManager().findPreference(notificationsKey);
        notificationPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                sharedPreferences.edit().putBoolean("notifications", Boolean.valueOf(newValue.toString()));
                return true;
            }
        });
    }

}
