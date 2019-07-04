package com.crackdress.wordgrab.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

import com.crackdress.wordgrab.R;

public class PreferencesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = PreferencesFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }


    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                    updatePreferenceSummary(preferenceGroup.getPreference(j));
                }
            } else {
                updatePreferenceSummary(preference);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Log.i(TAG, "onSharedPreferenceChanged");
     //   Toast.makeText(getActivity(), "Prefs changed..", Toast.LENGTH_LONG).show();
        updatePreferenceSummary(findPreference(key));
    }

    private void updatePreferenceSummary(Preference preference) {

        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
        }


        if (key != null && key.equals("AutoOptionsKey")) {
//            Log.i(TAG, "updatePreferenceSummary: " + ((ListPreference) preference).getValue());
            if (((ListPreference) preference).getValue().equals(getString(R.string.auto_no_contacts))) {
                getPreferenceScreen().findPreference("ContactsToRecordKey").setEnabled(true); //Enabling
            } else {
                getPreferenceScreen().findPreference("ContactsToRecordKey").setEnabled(false); //Disabling
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

