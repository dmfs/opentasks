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

package org.dmfs.tasks.notification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.notification.NotificationActionUtils.NotificationAction;
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.DateFormatter.DateFormatContext;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.Time;


/**
 * A {@link Service} that triggers and updates {@link Notification}s for Due and Start alarms as well as pinned tasks.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 *
 */
public class NotificationUpdaterService extends Service
{
	public static final String INTENT_ACTION_NEXT_DAY = "org.dmfs.tasks.intent.ACTION_DAY_CHANGED";
	public static final String INTENT_ACTION_PIN_TASK = "org.dmfs.tasks.intent.ACTION_PIN_TASK";
	public static final String INTENT_EXTRA_NEW_PINNED_TASK = "org.dmfs.intent.EXTRA_NEW_PINNED_TASK";
	private final NotificationCompat.Builder mBuilder = new Builder(this);
	private PendingIntent mDateChangePendingIntent;
	ArrayList<ContentSet> mTasksToPin;


	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}


	public NotificationUpdaterService()
	{
		super();
	}


	@Override
	public void onCreate()
	{
		updateNextDayAlarm();
		super.onCreate();
	}


	@Override
	public void onDestroy()
	{
		cancelNextDayAlarm();
		super.onDestroy();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{

		String intentAction = intent.getAction();
		if (intentAction != null)
		{
			switch (intentAction)
			{
				case INTENT_ACTION_PIN_TASK:
					// check for new task to pin
					if (intent.hasExtra(INTENT_EXTRA_NEW_PINNED_TASK))
					{
						ContentSet newTaskToPin = intent.getParcelableExtra(INTENT_EXTRA_NEW_PINNED_TASK);
						makePinNotification(this, mBuilder, newTaskToPin, true, true);
					}
					updateNotifications(false);
					break;

				case Intent.ACTION_BOOT_COMPLETED:
				case Intent.ACTION_REBOOT:
				case PinTaskHandler.ACTION_FASTBOOT:
					updateNotifications(true);
					break;

				case Intent.ACTION_DATE_CHANGED:
				case Intent.ACTION_TIME_CHANGED:
				case Intent.ACTION_TIMEZONE_CHANGED:
				case INTENT_ACTION_NEXT_DAY:
					updateNextDayAlarm();
					updateNotifications(false);
					break;

				default:
					updateNotifications(false);
					break;
			}
		}

		// check if the service needs to kept alive
		if (mTasksToPin == null || mTasksToPin.isEmpty())
		{
			this.stopSelf();
		}
		return Service.START_NOT_STICKY;

	}


	private void updateNotifications(boolean isReboot)
	{
		// update pinned tasks
		mTasksToPin = queryTasksToPin();
		updatePinnedNotifications(mTasksToPin, isReboot);

	}


	private void updatePinnedNotifications(ArrayList<ContentSet> tasksToPin, boolean isReboot)
	{
		ArrayList<Uri> pinnedTaskUris = PinTaskHandler.getPinnedTaskUris(this);

		// show notifications
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		for (ContentSet taskContentSet : tasksToPin)
		{
			boolean isAlreadyShown = pinnedTaskUris.contains(taskContentSet.getUri());
			Integer taskId = TaskFieldAdapters.TASK_ID.get(taskContentSet);
			notificationManager.notify(taskId, makePinNotification(this, mBuilder, taskContentSet, !isAlreadyShown, !isAlreadyShown));
		}

		// remove old notifications
		if (!isReboot)
		{
			for (Uri uri : pinnedTaskUris)
			{
				if (uri != null && uri.getLastPathSegment() != null && !containsTask(tasksToPin, uri))
				{

					Integer notificationId = Integer.valueOf(uri.getLastPathSegment());
					if (notificationId != null)
					{
						notificationManager.cancel(notificationId);
					}
				}
			}
		}
		PinTaskHandler.savePinnedTasks(this, tasksToPin);
	}


	private boolean containsTask(ArrayList<ContentSet> tasks, Uri taskUri)
	{
		for (ContentSet contentSet : tasks)
		{
			if (taskUri.equals(contentSet.getUri()))
			{
				return true;
			}
		}
		return false;
	}


	@SuppressLint("InlinedApi")
	public void resendPinNotification(Uri taskUri)
	{
		if (taskUri == null)
		{
			return;
		}
		String notificationIdString = taskUri.getLastPathSegment();
		if (notificationIdString == null)
		{
			return;
		}
		Integer notificationId = Integer.valueOf(notificationIdString);
		if (notificationId == null)
		{
			return;
		}
		mBuilder.setDefaults(Notification.DEFAULT_ALL);
		mBuilder.setOngoing(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			mBuilder.setPriority(Notification.PRIORITY_HIGH);
		}

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		notificationManager.notify(notificationId, mBuilder.build());
	}


	private ArrayList<ContentSet> queryTasksToPin()
	{
		ArrayList<ContentSet> tasksToPin = new ArrayList<ContentSet>(20);

		final ContentResolver resolver = this.getContentResolver();
		final Uri contentUri = Tasks.getContentUri(this.getString(R.string.org_dmfs_tasks_authority));
		final Cursor cursor = resolver.query(contentUri, new String[] { Tasks._ID, Tasks.TITLE, Tasks.DESCRIPTION, Tasks.DTSTART, Tasks.DUE, Tasks.IS_ALLDAY,
			Tasks.STATUS }, Tasks.PINNED + "= 1", null, Tasks.PRIORITY + " is not null, " + Tasks.PRIORITY + " DESC");
		try
		{
			if (cursor.moveToFirst())
			{
				do
				{
					Uri taskUri = ContentUris.withAppendedId(contentUri, TaskFieldAdapters.TASK_ID.get(cursor));
					ContentSet contentSet = new ContentSet(taskUri);
					contentSet.put(Tasks._ID, TaskFieldAdapters.TASK_ID.get(cursor));
					contentSet.put(Tasks.STATUS, TaskFieldAdapters.STATUS.get(cursor));
					contentSet.put(Tasks.TITLE, TaskFieldAdapters.TITLE.get(cursor));
					contentSet.put(Tasks.DESCRIPTION, TaskFieldAdapters.DESCRIPTION.get(cursor));
					contentSet.put(Tasks.DTSTART, cursor.getLong(cursor.getColumnIndex(Tasks.DTSTART)));
					contentSet.put(Tasks.DUE, cursor.getLong(cursor.getColumnIndex(Tasks.DUE)));
					contentSet.put(Tasks.IS_ALLDAY, cursor.getLong(cursor.getColumnIndex(Tasks.IS_ALLDAY)));
					tasksToPin.add(contentSet);

				} while (cursor.moveToNext());
			}
		}
		finally
		{
			cursor.close();
		}
		return tasksToPin;
	}


	private static Notification makePinNotification(Context context, Builder builder, ContentSet task, boolean withSound, boolean withTickerText)
	{
		// reset actions
		builder.mActions = new ArrayList<Action>(2);

		// content
		builder.setSmallIcon(R.drawable.ic_pin_white_24dp).setContentTitle(TaskFieldAdapters.TITLE.get(task)).setOngoing(true).setShowWhen(false)
			.setOnlyAlertOnce(true).setDefaults(Notification.DEFAULT_LIGHTS);

		// color
		builder.setColor(context.getResources().getColor(R.color.colorPrimary));

		// description
		String contentText = makePinNotificationContentText(context, task);
		if (contentText != null)
		{
			builder.setContentText(contentText);
		}

		// sound
		if (withSound)
		{
			builder.setDefaults(Notification.DEFAULT_ALL);
		}

		// ticker text
		if (withTickerText)
		{
			builder.setTicker(context.getString(R.string.notification_task_pin_ticker, (TaskFieldAdapters.TITLE.get(task))));
		}

		// click action
		Intent resultIntent = new Intent(Intent.ACTION_VIEW);
		resultIntent.setData(task.getUri());
		resultIntent.setPackage(context.getPackageName());

		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);

		// complete action
		Integer taskStatus = TaskFieldAdapters.STATUS.get(task);
		if (!(Tasks.STATUS_COMPLETED == taskStatus || Tasks.STATUS_CANCELLED == taskStatus))
		{
			Time dueTime = TaskFieldAdapters.DUE.get(task);
			long dueTimestamp = dueTime == null ? 0 : dueTime.toMillis(true);

			NotificationAction completeAction = new NotificationAction(NotificationActionIntentService.ACTION_COMPLETE, R.string.notification_action_completed,
				TaskFieldAdapters.TASK_ID.get(task), task.getUri(), dueTimestamp);
			builder.addAction(NotificationActionIntentService.getCompleteAction(context,
				NotificationActionUtils.getNotificationActionPendingIntent(context, completeAction)));
		}

		// unpin action
		builder.addAction(NotificationActionIntentService.getUnpinAction(context, TaskFieldAdapters.TASK_ID.get(task), task.getUri()));

		return builder.build();
	}


	private static String makePinNotificationContentText(Context context, ContentSet task)
	{
		boolean isAllDay = TaskFieldAdapters.ALLDAY.get(task);
		Time now = new Time();
		now.setToNow();
		Time start = TaskFieldAdapters.DTSTART.get(task);
		Time due = TaskFieldAdapters.DUE.get(task);

		if (start != null && start.toMillis(true) > 0 && (now.before(start) || due == null))
		{
			start.allDay = isAllDay;
			String startString = context.getString(R.string.notification_task_start_date,
				new DateFormatter(context).format(start, DateFormatContext.NOTIFICATION_VIEW));
			return startString;
		}

		if (due != null && due.toMillis(true) > 0)
		{
			due.allDay = isAllDay;
			String dueString = context.getString(R.string.notification_task_due_date,
				new DateFormatter(context).format(due, DateFormatContext.NOTIFICATION_VIEW));
			return dueString;
		}

		String description = TaskFieldAdapters.DESCRIPTION.get(task);
		if (description != null)
		{
			description = description.replaceAll("\\[\\s?\\]", " ").replaceAll("\\[[xX]\\]", "✓");

		}
		return description;
	}


	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void updateNextDayAlarm()
	{
		Intent intent = new Intent(this, NotificationUpdaterService.class);
		intent.setAction(INTENT_ACTION_NEXT_DAY);
		mDateChangePendingIntent = PendingIntent.getService(this, 0, intent, 0);

		// set alarm to update the next day
		GregorianCalendar tomorrow = new GregorianCalendar();
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		tomorrow.set(Calendar.HOUR_OF_DAY, 0);
		tomorrow.set(Calendar.MINUTE, 0);
		tomorrow.set(Calendar.SECOND, 0);
		tomorrow.set(Calendar.MILLISECOND, 0);

		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT)
		{
			alarmManager.setWindow(AlarmManager.RTC, tomorrow.getTimeInMillis(), 1000, mDateChangePendingIntent);
		}
		else
		{
			alarmManager.set(AlarmManager.RTC, tomorrow.getTimeInMillis(), mDateChangePendingIntent);
		}
	}


	private void cancelNextDayAlarm()
	{
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(mDateChangePendingIntent);
	}

}