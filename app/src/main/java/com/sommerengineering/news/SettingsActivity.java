package com.sommerengineering.news;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // initialize activity with superclass constructor
        super.onCreate(savedInstanceState);

        // layout has a single <fragment> tag
        setContentView(R.layout.settings_activity);
    }

    public static class ArticlePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {

            // initialize fragment with superclass constructor
            super.onCreate(savedInstanceState);

            // layout has a root PreferenceScreen with children EditTextPreference and ListPreference
            addPreferencesFromResource(R.xml.settings_main);

            // get preference key Strings
            String pageCountKey = getString(R.string.settings_min_magnitude_key);
            String orderByKey = getString(R.string.settings_order_by_key);

            // get the preference Objects associated with these keys
            Preference pageCount = findPreference(pageCountKey);
            Preference orderBy = findPreference(orderByKey);

            // set listeners on these preferences
            bindPreferenceSummaryToValue(pageCount);
            bindPreferenceSummaryToValue(orderBy);

        }

        private void bindPreferenceSummaryToValue(Preference preference) {

            // set listener on the preference
            preference.setOnPreferenceChangeListener(this);

            // get reference to the devices's shared preferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());

            // get the String key for the preference
            String preferenceKey = sharedPreferences.getString(preference.getKey(), "");

            // update preferences with the user defined values
            onPreferenceChange(preference, preferenceKey);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            // preference value is of type String
            String stringValue = value.toString();

            // the order-by preference is a list of Strings
            if (preference instanceof ListPreference) {

                // cast preference to the appropriate type
                ListPreference listPreference = (ListPreference) preference;

                // get index
                int prefIndex = listPreference.findIndexOfValue(stringValue);

                // simple check that we are indeed in a valid list
                if (prefIndex >= 0) {

                    // get String arrays defined in arrays.xml
                    CharSequence[] labels = listPreference.getEntries();

                    // setSummary updates the device's storage for app preferences
                    preference.setSummary(labels[prefIndex]);
                }

            }
            // the magnitude preference is a simple String
            else {

                // setSummary updates the device's storage for app preferences
                preference.setSummary(stringValue);
            }

            return true;

        }


    }

}
