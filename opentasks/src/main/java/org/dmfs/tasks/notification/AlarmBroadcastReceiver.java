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

import org.dmfs.provider.tasks.TaskContract;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;


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
		if (intent.getAction().equals(TaskContract.ACTION_BROADCAST_TASK_STARTING))
		{
			if (getAlarmPreference(context))
			{
				Uri taskUri = intent.getData();

				boolean silent = intent.getBooleanExtra(NotificationActionUtils.EXTRA_SILENT_NOTIFICATION, false);
				// check for pinned task
				if (TaskNotificationHandler.isTaskPinned(context, taskUri))
				{
					TaskNotificationHandler.sendPinnedTaskStartNotification(context, taskUri, silent);
					return;
				}

				// create regular notification
				String title = intent.getStringExtra(TaskContract.EXTRA_TASK_TITLE);
				long startDate = intent.getLongExtra(TaskContract.EXTRA_TASK_TIMESTAMP, Long.MIN_VALUE);
				boolean startAllDay = intent.getBooleanExtra(TaskContract.EXTRA_TASK_ALLDAY, false);
				String timezone = intent.getStringExtra(TaskContract.EXTRA_TASK_TIMEZONE);
				int notificationId = (int) ContentUris.parseId(taskUri);

				NotificationActionUtils.sendStartNotification(context, title, taskUri, notificationId, startDate, startAllDay, timezone, silent);

			}
		}
		else if (intent.getAction().equals(TaskContract.ACTION_BROADCAST_TASK_DUE))
		{
			if (getAlarmPreference(context))
			{
				Uri taskUri = intent.getData();

				boolean silent = intent.getBooleanExtra(NotificationActionUtils.EXTRA_SILENT_NOTIFICATION, false);

				// check for pinned task
				if (TaskNotificationHandler.isTaskPinned(context, taskUri))
				{
					TaskNotificationHandler.sendPinnedTaskDueNotification(context, taskUri, silent);
					return;
				}

				// create regular notification
				// long dueTime = intent.getLongExtra(AlarmNotificationHandler.EXTRA_TASK_DUE_TIME, System.currentTimeMillis());
				String title = intent.getStringExtra(TaskContract.EXTRA_TASK_TITLE);
				long dueDate = intent.getLongExtra(TaskContract.EXTRA_TASK_TIMESTAMP, Long.MIN_VALUE);
				boolean dueAllDay = intent.getBooleanExtra(TaskContract.EXTRA_TASK_ALLDAY, false);
				String timezone = intent.getStringExtra(TaskContract.EXTRA_TASK_TIMEZONE);
				int notificationId = (int) ContentUris.parseId(taskUri);

				NotificationActionUtils.sendDueAlarmNotification(context, title, taskUri, notificationId, dueDate, dueAllDay, timezone, silent);
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
