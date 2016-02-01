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
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.text.format.Time;
import android.widget.RemoteViews;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.TaskListActivity;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.DateFormatter.DateFormatContext;
import org.dmfs.tasks.utils.WidgetConfigurationDatabaseHelper;

import java.util.TimeZone;


/**
 * The provider for the small widget for legacy Android 2.x devices
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TaskListWidgetProviderLegacy extends AppWidgetProvider
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
			// on Android 2.x we update the widget directly
			onUpdate(context, appWidgetManager, appWidgetIds);
		}
	}


	protected ComponentName getComponentName(Context context)
	{
		return new ComponentName(context, TaskListWidgetProviderLegacy.class);
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
		String authority = TaskContract.taskAuthority(context);

		RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.task_list_widget);
		widget.removeAllViews(android.R.id.list);
		DateFormatter dateFormatter = new DateFormatter(context);
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(TaskContract.Instances.getContentUri(authority), null,
			TaskContract.Instances.VISIBLE + ">0 and " + TaskContract.Instances.IS_CLOSED + "=0 AND (" + TaskContract.Instances.INSTANCE_START + "<="
				+ System.currentTimeMillis() + " OR " + TaskContract.Instances.INSTANCE_START + " is null OR " + TaskContract.Instances.INSTANCE_START + " = "
				+ TaskContract.Instances.INSTANCE_DUE + " )",
			null, TaskContract.Instances.INSTANCE_DUE + " is null, " + TaskContract.Instances.DEFAULT_SORT_ORDER + ", " + TaskContract.Instances.PRIORITY
				+ " is null, " + TaskContract.Instances.PRIORITY + ", " + TaskContract.Instances.CREATED + " DESC");

		cursor.moveToFirst();
		int count = 0;
		Time now = new Time();
		now.clear(TimeZone.getDefault().getID());
		now.setToNow();
		now.normalize(true);
		Resources resources = context.getResources();
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
				dueDate.normalize(true);

				taskItem.setTextViewText(android.R.id.text1, dateFormatter.format(dueDate, DateFormatContext.WIDGET_VIEW));

				// highlight overdue dates & times
				if ((!dueDate.allDay && dueDate.before(now)
					|| dueDate.allDay && (dueDate.year < now.year || dueDate.yearDay <= now.yearDay && dueDate.year == now.year))
					&& !TaskFieldAdapters.IS_CLOSED.get(cursor))
				{
					taskItem.setTextColor(android.R.id.text1, resources.getColor(R.color.holo_red_light));
				}
				else
				{
					taskItem.setTextColor(android.R.id.text1, resources.getColor(R.color.lighter_gray));
				}
			}

			/*
			 * Create and set a {@link PendingIntent} to view the task when the list is clicked.
			 */
			Intent itemClickIntent = new Intent();
			itemClickIntent.setAction(Intent.ACTION_VIEW);
			itemClickIntent.putExtra(TaskListActivity.EXTRA_FORCE_LIST_SELECTION, true);
			itemClickIntent.setData(ContentUris.withAppendedId(Tasks.getContentUri(authority), TaskFieldAdapters.TASK_ID.get(cursor)));
			PendingIntent launchPI = PendingIntent.getActivity(context, 0, itemClickIntent, 0);
			taskItem.setOnClickPendingIntent(R.id.widget_list_item, launchPI);

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

		appWidgetManager.updateAppWidget(appWidgetIds, widget);
		return;
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
