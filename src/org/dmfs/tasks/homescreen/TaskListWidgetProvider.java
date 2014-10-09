/*
 * 
 *
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

package org.dmfs.tasks.homescreen;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.TaskListActivity;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.DueDateFormatter;
import org.dmfs.tasks.utils.WidgetConfigurationDatabaseHelper;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;


/**
 * The Class TaskListWidgetProvider.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TaskListWidgetProvider extends AppWidgetProvider
{
	private final static String TAG = "TaskListWidgetProvider";


	/*
	 * Override the onReceive method from the {@link BroadcastReceiver } class so that we can intercept broadcast for manual refresh of widget.
	 * 
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);

		String action = intent.getAction();
		if (action.equals(Intent.ACTION_PROVIDER_CHANGED))
		{
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(getComponentName(context));
			if (android.os.Build.VERSION.SDK_INT >= 11)
			{
				appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.task_list_widget_lv);
			}
			else
			{
				onUpdate(context, appWidgetManager, appWidgetIds);
			}
		}
	}


	static ComponentName getComponentName(Context context)
	{
		return new ComponentName(context, TaskListWidgetProvider.class);
	}


	/*
	 * This method is called periodically to update the widget.
	 * 
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@SuppressWarnings("deprecation")
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		String authority = context.getString(R.string.org_dmfs_tasks_authority);

		if (android.os.Build.VERSION.SDK_INT < 11)
		{
			RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.task_list_widget);
			widget.removeAllViews(android.R.id.list);
			DueDateFormatter dateFormatter = new DueDateFormatter(context);
			ContentResolver resolver = context.getContentResolver();
			Cursor cursor = resolver.query(TaskContract.Instances.getContentUri(authority), null, TaskContract.Instances.VISIBLE + ">0 and "
				+ TaskContract.Instances.IS_CLOSED + "=0 AND (" + TaskContract.Instances.INSTANCE_START + "<=" + System.currentTimeMillis() + " OR "
				+ TaskContract.Instances.INSTANCE_START + " is null)", null, TaskContract.Instances.INSTANCE_DUE + " is null, "
				+ TaskContract.Instances.DEFAULT_SORT_ORDER + ", " + TaskContract.Instances.PRIORITY + " is null, " + TaskContract.Instances.PRIORITY + ", "
				+ TaskContract.Instances.CREATED + " DESC");

			cursor.moveToFirst();
			int count = 0;
			while (!cursor.isAfterLast() && count < 7)
			{
				RemoteViews taskItem = new RemoteViews(context.getPackageName(), R.layout.task_list_widget_item);
				int taskColor = TaskFieldAdapters.LIST_COLOR.get(cursor);
				taskItem.setInt(R.id.task_list_color, "setBackgroundColor", taskColor);
				String title = TaskFieldAdapters.TITLE.get(cursor);
				taskItem.setTextViewText(android.R.id.title, title);
				Time dueDate = TaskFieldAdapters.DUE.get(cursor);
				if (dueDate != null)
				{
					taskItem.setTextViewText(android.R.id.text1, dateFormatter.format(dueDate));
				}
				widget.addView(android.R.id.list, taskItem);
				cursor.moveToNext();
				count++;
			}
			cursor.close();

			/** Add pending Intent to start the Tasks app when the title is clicked */
			Intent tasksAppIntent = new Intent(context, TaskListActivity.class);
			tasksAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent taskAppPI = PendingIntent.getActivity(context, 0, tasksAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setOnClickPendingIntent(android.R.id.button1, taskAppPI);

			/** Add a pending Intent to start new Task Activity on the new Task Button */
			Intent editTaskIntent = new Intent(Intent.ACTION_INSERT);
			editTaskIntent.setData(Tasks.getContentUri(authority));
			editTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent newTaskPI = PendingIntent.getActivity(context, 0, editTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setOnClickPendingIntent(android.R.id.button2, newTaskPI);

			/*
			 * Create and set a {@link PendingIntent} to launch the application when the list is clicked.
			 */
			Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
			PendingIntent launchPI = PendingIntent.getActivity(context, 0, launchIntent, 0);
			widget.setOnClickPendingIntent(android.R.id.list, launchPI);

			appWidgetManager.updateAppWidget(appWidgetIds, widget);
			return;
		}

		/*
		 * Iterate over all the widgets of this type and update them individually.
		 */
		for (int i = 0; i < appWidgetIds.length; i++)
		{
			Log.d(TAG, "updating widget " + i);

			/** Create an Intent with the {@link RemoteViewsService } and pass it the Widget Id */
			Intent remoteServiceIntent = new Intent(context, TaskListWidgetUpdaterService.class);
			remoteServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			remoteServiceIntent.setData(Uri.parse(remoteServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));

			RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.task_list_widget);

			/** Add pending Intent to start the Tasks app when the title is clicked */
			Intent tasksAppIntent = new Intent(context, TaskListActivity.class);
			tasksAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent taskAppPI = PendingIntent.getActivity(context, 0, tasksAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setOnClickPendingIntent(android.R.id.button1, taskAppPI);

			/** Add a pending Intent to start new Task Activity on the new Task Button */
			Intent editTaskIntent = new Intent(Intent.ACTION_INSERT);
			editTaskIntent.setData(Tasks.getContentUri(authority));
			editTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent newTaskPI = PendingIntent.getActivity(context, 0, editTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setOnClickPendingIntent(android.R.id.button2, newTaskPI);

			/** Set the {@link RemoteViewsService } subclass as the adapter for the {@link ListView} in the widget. */
			if (android.os.Build.VERSION.SDK_INT < 14)
			{
				widget.setRemoteAdapter(appWidgetIds[i], R.id.task_list_widget_lv, remoteServiceIntent);
			}
			else
			{
				widget.setRemoteAdapter(R.id.task_list_widget_lv, remoteServiceIntent);
			}
			appWidgetManager.notifyAppWidgetViewDataChanged(i, R.id.task_list_widget_lv);

			Intent detailIntent = new Intent(Intent.ACTION_VIEW);
			PendingIntent clickPI = PendingIntent.getActivity(context, 0, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			widget.setPendingIntentTemplate(R.id.task_list_widget_lv, clickPI);

			/* Finally update the widget */
			appWidgetManager.updateAppWidget(appWidgetIds[i], widget);
		}
	}


	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		// Delete configuration
		WidgetConfigurationDatabaseHelper dbHelper = new WidgetConfigurationDatabaseHelper(context);
		dbHelper.deleteWidgetConfiguration(appWidgetIds);

		super.onDeleted(context, appWidgetIds);
	}
}
