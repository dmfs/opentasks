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

import java.util.TimeZone;

import org.dmfs.provider.tasks.broadcast.DueAlarmBroadcastHandler;
import org.dmfs.provider.tasks.broadcast.StartAlarmBroadcastHandler;
import org.dmfs.tasks.R;
import org.dmfs.tasks.utils.DueDateFormatter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateUtils;
import android.text.format.Time;


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

	private final int NOTIFICATION_DATE_FORMAT = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_TIME;


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

				String startString = context.getString(R.string.notification_task_start_date,
					new DueDateFormatter(context, NOTIFICATION_DATE_FORMAT).format(makeTime(startDate, startAllDay), false));

				NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

				// build notification
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notification_completed)
					.setContentTitle(context.getString(R.string.notification_task_start_title, title)).setContentText(startString);

				// dismisses the notification on click
				mBuilder.setAutoCancel(true);

				// set status bar test
				mBuilder.setTicker(title);

				// enable light, sound and vibration
				mBuilder.setDefaults(Notification.DEFAULT_ALL);

				// Creates an explicit intent for an Activity in your app
				Intent resultIntent = new Intent(Intent.ACTION_VIEW);
				resultIntent.setData(intent.getData());

				// The stack builder object will contain an artificial back stack for the
				// started Activity.
				// This ensures that navigating backward from the Activity leads out of
				// your application to the Home screen.
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
				// Adds the Intent that starts the Activity to the top of the stack
				stackBuilder.addNextIntent(resultIntent);
				PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

				mBuilder.setContentIntent(resultPendingIntent);
				notificationManager.notify((int) taskId, mBuilder.build());

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

				String dueString = context.getString(R.string.notification_task_due_date,
					new DueDateFormatter(context, NOTIFICATION_DATE_FORMAT).format(makeTime(dueDate, dueAllDay), false));

				NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

				// build notification
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notification_completed)
					.setContentTitle(context.getString(R.string.notification_task_due_title, title)).setContentText(dueString);

				// dismisses the notification on click
				mBuilder.setAutoCancel(true);

				// set status bar test
				mBuilder.setTicker(title);

				// enable light, sound and vibration
				mBuilder.setDefaults(Notification.DEFAULT_ALL);

				// Creates an explicit intent for an Activity in your app
				Intent resultIntent = new Intent(Intent.ACTION_VIEW);
				resultIntent.setData(intent.getData());

				// The stack builder object will contain an artificial back stack for the
				// started Activity.
				// This ensures that navigating backward from the Activity leads out of
				// your application to the Home screen.
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
				// Adds the Intent that starts the Activity to the top of the stack
				stackBuilder.addNextIntent(resultIntent);
				PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

				// add actions
				mBuilder.addAction(NotificationActionIntentService.getCompleteAction(context, notificationId, taskId));
				if (!dueAllDay)
				{
					mBuilder.addAction(NotificationActionIntentService.getDelay1hAction(context, notificationId, taskId, dueDate, timezone));
				}
				mBuilder.addAction(NotificationActionIntentService.getDelay1dAction(context, notificationId, taskId, dueDate, timezone));

				mBuilder.setContentIntent(resultPendingIntent);
				notificationManager.notify(notificationId, mBuilder.build());

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


	private static Time makeTime(long timestamp, boolean allday)
	{
		Time result = new Time(allday ? Time.TIMEZONE_UTC : TimeZone.getDefault().getID());
		result.set(timestamp);
		result.allDay = allday;
		return result;
	}
}
