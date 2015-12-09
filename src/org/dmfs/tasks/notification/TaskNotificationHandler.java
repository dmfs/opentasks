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

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.TaskFieldAdapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;


/**
 * The PinTaskHandler simplifies the pinning and unpinning of tasks. Internally it manages the pin notification handling.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TaskNotificationHandler extends BroadcastReceiver
{
	@SuppressWarnings("unused")
	private static final String TAG = "PinTaskHandler";

	// HTC intent action for fastboot
	static final String ACTION_FASTBOOT = "com.htc.intent.action.QUICKBOOT_POWERON";

	static final String SHARED_PREFERENCE_KEY_PINNED_TASKS = "org.dmfs.sharedpreferences.pinnedtasks";


	/**
	 * Receives the a notification when the data in the provider changed.
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (getPinnedTaskCount(context) == 0)
		{
			return;
		}
		startPinnedTaskService(context, intent.getData(), intent.getAction(), null);
	}


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
		TaskNotificationHandler.savePinnedTask(context, task);
		TaskNotificationHandler.startPinnedTaskService(context, task.getUri(), NotificationUpdaterService.ACTION_PIN_TASK, task);
		TaskFieldAdapters.PINNED.set(task, true);
	}


	/**
	 * Unpins a task to remove the notification.
	 * 
	 * @param context
	 *            The activity context.
	 * @param task
	 *            The task {@link ContentSet}.
	 */
	public static void unpinTask(Context context, ContentSet task)
	{
		TaskFieldAdapters.PINNED.set(task, false);
	}


	private static void startPinnedTaskService(Context context, Uri taskUri, String action, ContentSet task)
	{
		Intent intent = new Intent(context, NotificationUpdaterService.class);
		intent.setData(taskUri);
		intent.setAction(action);
		intent.putExtra(NotificationUpdaterService.EXTRA_NEW_PINNED_TASK, task);
		context.startService(intent);
	}


	private static int getPinnedTaskCount(Context context)
	{
		final Cursor countCursor = context.getContentResolver().query(Tasks.getContentUri(TaskContract.taskAuthority(context)),
			new String[] { "count(*) AS count" }, Tasks.PINNED + " is not null", null, null);
		try
		{
			countCursor.moveToFirst();
			return countCursor.getInt(0);
		}
		finally
		{
			countCursor.close();
		}
	}


	public static boolean isTaskPinned(Context context, Uri taskUri)
	{
		for (Uri uri : getPinnedTaskUris(context))
		{
			if (uri.equals(taskUri))
			{
				return true;
			}
		}
		return false;
	}


	/**
	 * Overrides the saved pinned tasks in the {@link SharedPreferences}.
	 * 
	 * @param context
	 * @param pinnedTasks
	 */
	static void savePinnedTasks(Context context, ArrayList<ContentSet> pinnedTasks)
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
	private static void savePinnedTask(Context context, ContentSet pinnedTask)
	{
		String pinnedTasks = PreferenceManager.getDefaultSharedPreferences(context).getString(SHARED_PREFERENCE_KEY_PINNED_TASKS, null);
		pinnedTasks += ",," + (pinnedTask.getUri().toString());
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(SHARED_PREFERENCE_KEY_PINNED_TASKS, pinnedTasks).commit();
	}


	static ArrayList<Uri> getPinnedTaskUris(Context context)
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


	public static void sendPinnedTaskStartNotification(Context context, Uri taskUri, boolean silent)
	{
		Intent intent = new Intent(context, NotificationUpdaterService.class);
		intent.setAction(NotificationUpdaterService.ACTION_PINNED_TASK_START);
		intent.putExtra(NotificationActionUtils.EXTRA_SILENT_NOTIFICATION, silent);
		intent.setData(taskUri);
		context.startService(intent);
	}


	public static void sendPinnedTaskDueNotification(Context context, Uri taskUri, boolean silent)
	{
		Intent intent = new Intent(context, NotificationUpdaterService.class);
		intent.setAction(NotificationUpdaterService.ACTION_PINNED_TASK_DUE);
		intent.putExtra(NotificationActionUtils.EXTRA_SILENT_NOTIFICATION, silent);
		intent.setData(taskUri);
		context.startService(intent);
	}

}
