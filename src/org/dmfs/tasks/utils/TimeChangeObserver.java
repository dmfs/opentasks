/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;


/**
 * A helper that listens for time and time zone changes and notifies a {@link TimeChangeListener} in case of a change event. In addition it allows to set an
 * alarm time and the listener will be notified when the alarm is triggered.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TimeChangeObserver extends BroadcastReceiver
{
	private static final String TAG = "TimeChangeObserver";

	private final TimeChangeListener mListener;
	private Handler mHandler;
	private final Context mAppContext;


	/**
	 * Creates a new {@link TimeChangeObserver}.
	 * 
	 * @param context
	 *            A {@link Context}
	 * @param listener
	 *            The {@link TimeChangeListener} to notify of any events,
	 */
	public TimeChangeObserver(Context context, TimeChangeListener listener)
	{
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
		intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		mAppContext = context.getApplicationContext();
		mAppContext.registerReceiver(this, intentFilter);
		mListener = listener;
	}


	public void releaseReceiver()
	{
		try
		{
			mAppContext.unregisterReceiver(this);
		}
		catch (IllegalArgumentException e)
		{
			Log.w(TAG, "Caught IllegalArgumentException no receiver was registered - instance " + this);
		}

	}


	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (mListener != null)
		{
			// notify the listener that the time or the time zone has been changed.
			mListener.onTimeUpdate(this);
		}
	}


	/**
	 * Set an alarm time. Any previously set alarm will be discarded, that means only one alarm can be set at a time.
	 * 
	 * @param alarm
	 *            The time in milliseconds since the epoch when to trigger the alarm.
	 */
	public void setNextAlarm(long alarm)
	{
		if (mHandler == null)
		{
			mHandler = new Handler();
		}

		// set new callback at the specified alarm
		mHandler.removeCallbacks(mNotifier);
		mHandler.postAtTime(mNotifier, alarm - System.currentTimeMillis() + android.os.SystemClock.uptimeMillis());
	}


	/**
	 * Set an alarm time. Any previously set alarm will be discarded, that means only one alarm can be set at a time.
	 * 
	 * @param alarm
	 *            The time when to trigger the alarm.
	 */
	public void setNextAlarm(Time alarm)
	{
		setNextAlarm(alarm.toMillis(false));
	}

	/**
	 * A {@link Runnable} that notifies our listener when the alarm is triggered.
	 */
	private final Runnable mNotifier = new Runnable()
	{
		@Override
		public void run()
		{
			if (mListener != null)
			{
				mListener.onAlarm(TimeChangeObserver.this);
			}
		}
	};
}
