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

package org.dmfs.tasks.notification;

import org.dmfs.provider.tasks.broadcast.DueAlarmBroadcastHandler;
import org.dmfs.provider.tasks.broadcast.StartAlarmBroadcastHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


/**
 * A {@link BroadcastReceiver} to handle incoming alarms for tasks.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver
{

	private static String PREFS_NAME = "alarm_preferences";
	private static String PREF_ALARM_ACTIVATED = "preference_alarm_activated";


	/**
	 * Is called on an incoming alarm broadcast. Creates a notifications for this alarm.
	 * 
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// continue if alarms where enabled
		if (intent.getAction().equals(StartAlarmBroadcastHandler.BROADCAST_START_ALARM))
		{
			if (getAlarmPreference(context))
			{
				long taskId = intent.getLongExtra(StartAlarmBroadcastHandler.EXTRA_TASK_ID, 0);
				// long dueTime = intent.getLongExtra(AlarmNotificationHandler.EXTRA_TASK_DUE_TIME, System.currentTimeMillis());
				String title = intent.getStringExtra(StartAlarmBroadcastHandler.EXTRA_TASK_TITLE);
				long startDate = intent.getLongExtra(StartAlarmBroadcastHandler.EXTRA_TASK_START_TIME, Long.MIN_VALUE);
				boolean startAllDay = intent.getBooleanExtra(StartAlarmBroadcastHandler.EXTRA_TASK_START_ALLDAY, false);
				int notificationId = (int) taskId;

				NotificationActionUtils.sendStartNotification(context, title, intent.getData(), notificationId, taskId, startDate, startAllDay);

			}
		}
		else if (intent.getAction().equals(DueAlarmBroadcastHandler.BROADCAST_DUE_ALARM))
		{
			if (getAlarmPreference(context))
			{
				long taskId = intent.getLongExtra(DueAlarmBroadcastHandler.EXTRA_TASK_ID, 0);
				// long dueTime = intent.getLongExtra(AlarmNotificationHandler.EXTRA_TASK_DUE_TIME, System.currentTimeMillis());
				String title = intent.getStringExtra(DueAlarmBroadcastHandler.EXTRA_TASK_TITLE);
				long dueDate = intent.getLongExtra(DueAlarmBroadcastHandler.EXTRA_TASK_DUE_TIME, Long.MIN_VALUE);
				boolean dueAllDay = intent.getBooleanExtra(DueAlarmBroadcastHandler.EXTRA_TASK_DUE_ALLDAY, false);
				String timezone = intent.getStringExtra(DueAlarmBroadcastHandler.EXTRA_TASK_TIMEZONE);
				int notificationId = (int) taskId;

				NotificationActionUtils.sendDueAlarmNotification(context, title, intent.getData(), notificationId, taskId, dueDate, dueAllDay, timezone);
			}
		}

	}


	public static void setAlarmPreference(Context context, boolean value)
	{
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREF_ALARM_ACTIVATED, value);
		editor.commit();

	}


	public static boolean getAlarmPreference(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		return settings.getBoolean(PREF_ALARM_ACTIVATED, true);

	}
}
