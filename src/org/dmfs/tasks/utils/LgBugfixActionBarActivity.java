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

package org.dmfs.tasks.utils;

import java.lang.reflect.Field;

import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewConfiguration;


/**
 * This class provide a workaround for LG 4.1 devices with a bug which causes a NullPointerException when pressing the menu key.
 * 
 * RedMine Ticket #1551.
 * 
 * @see https://code.google.com/p/android/issues/detail?id=78154
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */

public class LgBugfixActionBarActivity extends ActionBarActivity
{

	private static final String TAG = "LgBugfixActionBarActivity";


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (isMenuWorkaroundRequired())
		{
			forceOverflowMenu();
		}
		super.onCreate(savedInstanceState);
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		return (keyCode == KeyEvent.KEYCODE_MENU && isMenuWorkaroundRequired()) || super.onKeyDown(keyCode, event);
	}


	@Override
	public boolean onKeyUp(int keyCode, @NonNull KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU && isMenuWorkaroundRequired())
		{
			openOptionsMenu();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}


	public static boolean isMenuWorkaroundRequired()
	{
		return VERSION.SDK_INT < VERSION_CODES.KITKAT && VERSION.SDK_INT > VERSION_CODES.GINGERBREAD_MR1
			&& ("LGE".equalsIgnoreCase(Build.MANUFACTURER) || "E6710".equalsIgnoreCase(Build.DEVICE));
	}


	/**
	 * Modified from: http://stackoverflow.com/a/13098824
	 */
	private void forceOverflowMenu()
	{
		try
		{
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null)
			{
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		}
		catch (NoSuchFieldException e)
		{
			Log.w(TAG, "Failed to force overflow menu.");
		}
		catch (IllegalAccessException e)
		{
			Log.w(TAG, "Failed to force overflow menu.");
		}
	}
}
