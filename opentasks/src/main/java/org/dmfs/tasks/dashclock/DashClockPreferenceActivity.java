/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
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
 * 
 */

package org.dmfs.tasks.dashclock;

import org.dmfs.tasks.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class DashClockPreferenceActivity extends PreferenceActivity
{
	public static final String KEY_PREF_DISPLAY_MODE = "pref_db_displayed_tasks";
	public static final int DISPLAY_MODE_ALL = 1;
	public static final int DISPLAY_MODE_DUE = 2;
	public static final int DISPLAY_MODE_START = 3;
	public static final int DISPLAY_MODE_PINNED = 4;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.dashclock_preferences);
	}
}
