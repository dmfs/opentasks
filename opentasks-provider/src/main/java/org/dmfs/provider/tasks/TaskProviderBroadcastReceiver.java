/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.provider.tasks;

import java.util.TimeZone;

import org.dmfs.rfc5545.DateTime;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


/**
 * A receiver for all task provider related broadcasts. This receiver merely forwards all incoming broadcasts to the provider, so they can be handled
 * asynchronously in the provider context.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TaskProviderBroadcastReceiver extends BroadcastReceiver
{
	private final static int REQUEST_CODE_ALARM = 1337;

	private final static String ACTION_NOTIFICATION_ALARM = "org.dmfs.tasks.provider.NOTIFICATION_ALARM";


	/**
	 * Registers a system alarm to update notifications at a specific time.
	 * 
	 * @param context
	 *            A Context.
	 * @param updateTime
	 *            When to fire the alarm.
	 */
	@SuppressLint("NewApi")
	static void planNotificationUpdate(Context context, DateTime updateTime)
	{
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(context, TaskProviderBroadcastReceiver.class);
		alarmIntent.setAction(ACTION_NOTIFICATION_ALARM);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_ALARM, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// cancel any previous alarm
		am.cancel(pendingIntent);

		if (updateTime.isFloating())
		{
			// convert floating times to absolute times
			updateTime = new DateTime(TimeZone.getDefault(), updateTime.getYear(), updateTime.getMonth(), updateTime.getDayOfMonth(), updateTime.getHours(),
				updateTime.getMinutes(), updateTime.getSeconds());
		}

		// AlarmManager API changed in v19 (KitKat) and the "set" method is not called at the exact time anymore
		if (Build.VERSION.SDK_INT > 18)
		{
			am.setExact(AlarmManager.RTC_WAKEUP, updateTime.getTimestamp(), pendingIntent);
		}
		else
		{
			am.set(AlarmManager.RTC_WAKEUP, updateTime.getTimestamp(), pendingIntent);
		}
	}


	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		switch (action)
		{
			case Intent.ACTION_TIMEZONE_CHANGED:
			{
				// the local timezone has been changed, notify the provider to take the necessary steps.
				// don't trigger the notifications update yet, because the timezone update will run asynhronously and we need to wait till that's finished
				ContentOperation.UPDATE_TIMEZONE.fire(context, null);
			}
			case ACTION_NOTIFICATION_ALARM:
			{
				// it's time for the next notification
				ContentOperation.POST_NOTIFICATIONS.fire(context, null);
			}
			default:
			{
				// at this time all other actions trigger an update of the notification alarm
				ContentOperation.UPDATE_NOTIFICATION_ALARM.fire(context, null);
			}
		}
	}
}
