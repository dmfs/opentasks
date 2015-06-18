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

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.TaskFieldAdapters;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;


/**
 * The PinTaskHandler simplifies the pinning and unpinning of tasks. Internally it manages the pin notification handling.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class PinTaskHandler extends BroadcastReceiver
{
	@SuppressWarnings("unused")
	private static final String TAG = "PinTaskHandler";

	// HTC intent action for fastboot
	private static final String ACTION_FASTBOOT = "com.htc.intent.action.QUICKBOOT_POWERON";

	private static final String SHARED_PREFERENCE_KEY_PINNED_TASKS = "org.dmfs.sharedpreferences.pinnedtasks";


	/**
	 * Pins a task as notification.
	 * 
	 * @param context
	 *            The activity context.
	 * @param task
	 *            The task {@link ContentSet}.
	 */
	public static void pinTask(Context context, ContentSet task)
	{
		PinTaskHandler.makeNotification(context, task, false, true);
		PinTaskHandler.savePinnedTask(context, task);
		TaskFieldAdapters.PINNED.set(task, true);
		task.persist(context);
	}


	/**
	 * Unpins a task and removes the notification.
	 * 
	 * @param context
	 *            The activity context.
	 * @param task
	 *            The task {@link ContentSet}.
	 */
	public static void unpinTask(Context context, ContentSet task)
	{
		TaskFieldAdapters.PINNED.set(task, false);
		task.persist(context);
	}


	/**
	 * Receives the a notification when the data in the provider changed.
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		boolean isReboot = Intent.ACTION_BOOT_COMPLETED == intent.getAction() || Intent.ACTION_REBOOT == intent.getAction()
			|| ACTION_FASTBOOT == intent.getAction();
		final ArrayList<ContentSet> tasksToPin = queryTasksToPin(context);
		updatePinnedNotifications(context, tasksToPin, isReboot);
	}


	private void updatePinnedNotifications(Context context, ArrayList<ContentSet> tasksToPin, boolean isReboot)
	{
		ArrayList<Uri> pinnedTaskUris = getPinnedTaskUris(context);

		// show notifications
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		for (ContentSet taskContentSet : tasksToPin)
		{
			boolean isAlreadyShown = pinnedTaskUris.contains(taskContentSet.getUri());
			Integer taskId = TaskFieldAdapters.TASK_ID.get(taskContentSet);
			notificationManager.notify(taskId, makeNotification(context, taskContentSet, !isAlreadyShown, !isAlreadyShown));
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
		savePinnedTasks(context, tasksToPin);
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


	public static Notification makeNotification(Context context, ContentSet task, boolean withSound, boolean withTickerText)
	{
		// content
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_pin_white_24dp)
			.setContentTitle(TaskFieldAdapters.TITLE.get(task)).setOngoing(true).setShowWhen(false).setOnlyAlertOnce(true);

		// description
		String description = TaskFieldAdapters.DESCRIPTION.get(task);
		if (description != null)
		{
			description = description.replaceAll("\\[\\s?\\]", " ").replaceAll("\\[[xX]\\]", "✓");
			mBuilder.setContentText(description);
		}

		// sound
		if (withSound)
		{
			Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			mBuilder.setSound(uri);
		}

		// ticker text
		if (withTickerText)
		{
			mBuilder.setTicker(context.getString(R.string.notification_task_pin_ticker, (TaskFieldAdapters.TITLE.get(task))));
		}

		// click action
		Intent resultIntent = new Intent(Intent.ACTION_VIEW);
		resultIntent.setData(task.getUri());
		resultIntent.setPackage(context.getPackageName());

		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		// unpin action
		mBuilder.addAction(NotificationActionIntentService.getUnpinAction(context, TaskFieldAdapters.TASK_ID.get(task), task.getUri()));

		// complete action
		Integer taskStatus = TaskFieldAdapters.STATUS.get(task);
		if (!(Tasks.STATUS_COMPLETED == taskStatus || Tasks.STATUS_CANCELLED == taskStatus))
		{
			mBuilder.addAction(NotificationActionIntentService.getCompleteAction(context, TaskFieldAdapters.TASK_ID.get(task), task.getUri()));
		}

		return mBuilder.build();
	}


	/**
	 * Overrides the saved pinned tasks in the {@link SharedPreferences}.
	 * 
	 * @param context
	 * @param pinnedTasks
	 */
	private void savePinnedTasks(Context context, ArrayList<ContentSet> pinnedTasks)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pinnedTasks.size(); i++)
		{
			sb.append(pinnedTasks.get(i).getUri().toString());
			if (i < pinnedTasks.size() - 1)
			{
				sb.append(",,");
			}
		}
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(SHARED_PREFERENCE_KEY_PINNED_TASKS, sb.toString()).commit();
	}


	/**
	 * Adds the task to the saved pinned tasks in the {@link SharedPreferences}.
	 * 
	 * @param context
	 * @param pinnedTask
	 */
	public static void savePinnedTask(Context context, ContentSet pinnedTask)
	{
		String pinnedTasks = PreferenceManager.getDefaultSharedPreferences(context).getString(SHARED_PREFERENCE_KEY_PINNED_TASKS, null);
		pinnedTasks += ",," + (pinnedTask.getUri().toString());
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(SHARED_PREFERENCE_KEY_PINNED_TASKS, pinnedTasks).commit();
	}


	private ArrayList<Uri> getPinnedTaskUris(Context context)
	{
		final ArrayList<Uri> pinnedTaskUris = new ArrayList<Uri>(20);
		final String pinnedTasks = PreferenceManager.getDefaultSharedPreferences(context).getString(SHARED_PREFERENCE_KEY_PINNED_TASKS, null);
		if (pinnedTasks == null)
		{
			return pinnedTaskUris;
		}
		for (String uriString : pinnedTasks.split(",,"))
		{
			pinnedTaskUris.add(Uri.parse(uriString));
		}
		return pinnedTaskUris;
	}


	private static ArrayList<ContentSet> queryTasksToPin(Context context)
	{
		ArrayList<ContentSet> tasksToPin = new ArrayList<ContentSet>(20);

		final ContentResolver resolver = context.getContentResolver();
		final Uri contentUri = Tasks.getContentUri(context.getString(R.string.org_dmfs_tasks_authority));
		final Cursor cursor = resolver.query(contentUri, new String[] { Tasks._ID, Tasks.TITLE, Tasks.DESCRIPTION, Tasks.DTSTART, Tasks.DUE, Tasks.STATUS },
			Tasks.PINNED + "= 1", null, Tasks.PRIORITY + " DESC");
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
}
