/*
 * Copyright 2017 dmfs GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dmfs.tasks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import org.dmfs.tasks.utils.BaseActivity;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;


/**
 * Activity for the general app settings screen.
 *
 * @author Gabor Keszthelyi
 */
public final class AppSettingsActivity extends BaseActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
{
    private SharedPreferences mPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.opentasks_activity_preferences);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, new AppSettingsFragment())
                    .commit();
        }
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref)
    {
        if (Build.VERSION.SDK_INT >= 26 && "notifications".equalsIgnoreCase(pref.getKey()))
        {
            //  open the system notification settings
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
            return true;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), pref.getFragment()))
                .addToBackStack("")
                .commit();
        return true;
    }


    @Override
    public Resources.Theme getTheme()
    {
        Resources.Theme theme = super.getTheme();
        if (Build.VERSION.SDK_INT < 29)
        {
            theme.applyStyle(
                    mPrefs.getBoolean(
                            getString(R.string.opentasks_pref_appearance_dark_theme),
                            getResources().getBoolean(R.bool.opentasks_dark_theme_default)) ?
                            R.style.OpenTasks_Theme_Dark :
                            R.style.OpenTasks_Theme_Light,
                    true);
        }
        return theme;
    }
}
