/*
 * Copyright (C) 2012 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Spannable;
import android.util.Log;
import android.widget.EditText;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.widget.SeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class NavBar extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "NavBarSettings";

    private static final String KEY_SOFT_KEYS = "pref_soft_keys";
    private static final String KEY_NAV_BAR_EDITOR = "navigation_bar";
    private static final String NAVIGATION_BUTTON_COLOR = "navigation_button_color";
    private static final String PREF_NAV_GLOW_COLOR = "nav_button_glow_color";

    private CheckBoxPreference mSoftKeys;
    private PreferenceScreen mNavBarEditor;
    private ColorPickerPreference mNavButtonColor;
    private ListPreference mGlowTimes;

    	ListPreference mNavigationBarHeight;
    	ListPreference mNavigationBarWidth;
        SeekBarPreference mButtonAlpha;
        ColorPickerPreference mNavigationBarGlowColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPreferenceManager() != null) {
            addPreferencesFromResource(R.xml.navbar_settings);

            PreferenceScreen prefSet = getPreferenceScreen();

            mSoftKeys = (CheckBoxPreference) prefSet.findPreference(KEY_SOFT_KEYS);
            mSoftKeys.setChecked(Settings.System.getInt(getActivity().getContentResolver(), 
                    Settings.System.SOFT_KEYS, 0) == 1);

            mNavBarEditor = (PreferenceScreen) prefSet.findPreference(KEY_NAV_BAR_EDITOR);
            mNavBarEditor.setEnabled(mSoftKeys.isChecked());

            mNavigationBarHeight = (ListPreference) findPreference("navigation_bar_height");
            mNavigationBarHeight.setOnPreferenceChangeListener(this);

            mNavigationBarWidth = (ListPreference) findPreference("navigation_bar_width");
            mNavigationBarWidth.setOnPreferenceChangeListener(this);

            mNavButtonColor = (ColorPickerPreference) prefSet.findPreference(NAVIGATION_BUTTON_COLOR);
            mNavButtonColor.setOnPreferenceChangeListener(this);

            mNavigationBarGlowColor = (ColorPickerPreference) findPreference(PREF_NAV_GLOW_COLOR);
            mNavigationBarGlowColor.setOnPreferenceChangeListener(this);

            mGlowTimes = (ListPreference) prefSet.findPreference("glow_times");
            mGlowTimes.setOnPreferenceChangeListener(this);

            float defaultAlpha = Settings.System.getFloat(getActivity()
                    .getContentResolver(), Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                    0.6f);
            mButtonAlpha = (SeekBarPreference) findPreference("button_transparency");
            mButtonAlpha.setInitValue((int) (defaultAlpha * 100));
            mButtonAlpha.setOnPreferenceChangeListener(this);
            if (Utils.isScreenLarge())
                prefSet.removePreference(mNavBarEditor);
                prefSet.removePreference(mNavigationBarWidth);
                prefSet.removePreference(mNavigationBarHeight);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSoftKeys){
            boolean mValue = mSoftKeys.isChecked();
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SOFT_KEYS, mValue ? 1 : 0);
            mNavBarEditor.setEnabled(mSoftKeys.isChecked());
            return true;
	}
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mGlowTimes) {
            int breakIndex = ((String) newValue).indexOf("|");
            String value = (String) newValue;

            int offTime = Integer.parseInt(value.substring(breakIndex + 1));
            int onTime = Integer.parseInt(value.substring(0, breakIndex));

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_DURATION[0],
                    offTime);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_DURATION[1],
                    onTime);
            return true;
        } else if (preference == mButtonAlpha) {
            float val = Float.parseFloat((String) newValue);
            Log.e("R", "value: " + val / 100);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                    val / 100);
            return true;
        } else if (preference == mNavButtonColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                .valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BUTTON_COLOR, intHex);
            return true;
        } else if (preference == mNavigationBarGlowColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_TINT, intHex);
            return true;
        } else if (preference == mNavigationBarWidth) {
		    String newVal = (String) newValue;
		    int dp = Integer.parseInt(newVal);
		    int width = mapChosenDpToPixels(dp);
		    Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_WIDTH,
		            width);
		    mNavBarEditor.setEnabled(mSoftKeys.isChecked());
		    return true;
		} else if (preference == mNavigationBarHeight) {
		    String newVal = (String) newValue;
		    int dp = Integer.parseInt(newVal);
		    int height = mapChosenDpToPixels(dp);
		    Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_HEIGHT,
		            height);
		    mNavBarEditor.setEnabled(mSoftKeys.isChecked());
		    return true;
		}
		return false;
	}
        public int mapChosenDpToPixels(int dp) {
            switch (dp) {
                case 48:
                    return getResources().getDimensionPixelSize(R.dimen.navigation_bar_48);
                case 42:
                    return getResources().getDimensionPixelSize(R.dimen.navigation_bar_42);
                case 36:
                    return getResources().getDimensionPixelSize(R.dimen.navigation_bar_36);
                case 30:
                    return getResources().getDimensionPixelSize(R.dimen.navigation_bar_30);
                case 24:
                    return getResources().getDimensionPixelSize(R.dimen.navigation_bar_24);
            }
            return -1;
        }
}
