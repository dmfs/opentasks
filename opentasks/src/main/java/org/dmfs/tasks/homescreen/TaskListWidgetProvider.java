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

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.TaskListActivity;
import org.dmfs.tasks.utils.WidgetConfigurationDatabaseHelper;


/**
 * The provider for the widget on Android Honeycomb and up.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Tobias Reinsch <tobias@dmfs.org>
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

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(getComponentName(context));
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_PROVIDER_CHANGED))
		{
			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.task_list_widget_lv);
		}
	}


	protected ComponentName getComponentName(Context context)
	{
		return new ComponentName(context, TaskListWidgetProvider.class);
	}


	/*
	 * This method is called periodically to update the widget.
	 * 
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		String authority = TaskContract.taskAuthority(context);

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

			Intent detailIntent = new Intent(Intent.ACTION_VIEW);
			detailIntent.putExtra(TaskListActivity.EXTRA_FORCE_LIST_SELECTION, true);
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
