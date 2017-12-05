/*
 * Copyright 2017 dmfs GmbH
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
 */

package org.dmfs.tasks.homescreen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import org.dmfs.provider.tasks.AuthorityUtil;
import org.dmfs.tasks.EditTaskActivity;
import org.dmfs.tasks.R;
import org.dmfs.tasks.TaskListActivity;
import org.dmfs.tasks.contract.TaskContract.TaskLists;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.utils.RecentlyUsedLists;
import org.dmfs.tasks.utils.WidgetConfigurationDatabaseHelper;

import java.util.ArrayList;


/**
 * The provider for the widget on Android Honeycomb and up.
 *
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TaskListWidgetProvider extends AppWidgetProvider
{
    private final static String TAG = "TaskListWidgetProvider";
    public static String ACTION_CREATE_TASK = "CreateTask";


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
        else if (action.equals(ACTION_CREATE_TASK))
        {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            WidgetConfigurationDatabaseHelper configHelper = new WidgetConfigurationDatabaseHelper(context);
            SQLiteDatabase db = configHelper.getReadableDatabase();
            ArrayList<Long> widgetLists = WidgetConfigurationDatabaseHelper.loadTaskLists(db, widgetId);
            db.close();
            ArrayList<Long> writableLists = new ArrayList<>();
            String authority = AuthorityUtil.taskAuthority(context);
            if (!widgetLists.isEmpty())
            {
                Cursor cursor = context.getContentResolver().query(
                        TaskLists.getContentUri(authority),
                        new String[] { TaskLists._ID },
                        TaskLists.SYNC_ENABLED + "=1 AND " + TaskLists._ID + " IN (" + TextUtils.join(",", widgetLists) + ")",
                        null,
                        null);
                if (cursor != null)
                {
                    while (cursor.moveToNext())
                    {
                        writableLists.add(cursor.getLong(0));
                    }
                    cursor.close();
                }
            }
            Intent editTaskIntent = new Intent(Intent.ACTION_INSERT);
            editTaskIntent.setData(Tasks.getContentUri(authority));
            editTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!writableLists.isEmpty())
            {
                Long preselectList;
                if (writableLists.size() == 1)
                {
                    // if there is only one list, then select this one
                    preselectList = writableLists.get(0);
                }
                else
                {
                    // if there are multiple lists, then select the most recently used
                    preselectList = RecentlyUsedLists.getRecentFromList(context, writableLists);
                }
                Log.d(getClass().getSimpleName(), "create task with preselected list " + preselectList);
                ContentSet contentSet = new ContentSet(Tasks.getContentUri(authority));
                contentSet.put(Tasks.LIST_ID, preselectList);
                Bundle extraBundle = new Bundle();
                extraBundle.putParcelable(EditTaskActivity.EXTRA_DATA_CONTENT_SET, contentSet);
                editTaskIntent.putExtra(EditTaskActivity.EXTRA_DATA_BUNDLE, extraBundle);
            }
            context.startActivity(editTaskIntent);
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
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
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
            Intent editTaskIntent = new Intent(context, TaskListWidgetProvider.class);
            editTaskIntent.setAction(ACTION_CREATE_TASK);
            editTaskIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            PendingIntent newTaskPI = PendingIntent.getBroadcast(context, appWidgetIds[i], editTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(android.R.id.button2, newTaskPI);

            /** Set the {@link RemoteViewsService } subclass as the adapter for the {@link ListView} in the widget. */
            widget.setRemoteAdapter(R.id.task_list_widget_lv, remoteServiceIntent);

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
