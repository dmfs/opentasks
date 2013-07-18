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
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.DueDateFormatter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;
import android.widget.RemoteViews;


/**
 * The Class TaskListWidgetProvider.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 */
public class TaskListWidgetProvider extends AppWidgetProvider
{

	/*
	 * This method is called periodically to update the widget.
	 * 
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
	 */

	@SuppressWarnings("deprecation")
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		if (android.os.Build.VERSION.SDK_INT < 11)
		{
			RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.task_list_widget);
			DueDateFormatter dateFormatter = new DueDateFormatter(context);
			ContentResolver resolver = context.getContentResolver();
			Cursor cursor = resolver.query(TaskContract.Instances.CONTENT_URI, null, TaskContract.Instances.VISIBLE + ">0 and "
				+ TaskContract.Instances.IS_CLOSED + "=0 AND (" + TaskContract.Instances.INSTANCE_START + "<=" + System.currentTimeMillis() + " OR "
				+ TaskContract.Instances.INSTANCE_START + " is null)", null, TaskContract.Instances.DEFAULT_SORT_ORDER);

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
			/** Create an Intent with the {@link RemoteViewsService } and pass it the Widget Id */
			Intent remoteServiceIntent = new Intent(context, TaskListWidgetUpdaterService.class);
			remoteServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			remoteServiceIntent.setData(Uri.parse(remoteServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));

			RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.task_list_widget);

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
			PendingIntent clickPI = PendingIntent.getActivity(context, 0, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			widget.setPendingIntentTemplate(R.id.task_list_widget_lv, clickPI);

			/* Finally update the widget */
			appWidgetManager.updateAppWidget(appWidgetIds[i], widget);
		}

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
