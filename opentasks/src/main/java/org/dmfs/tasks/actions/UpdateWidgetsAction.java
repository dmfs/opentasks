/*
 * Copyright 2020 dmfs GmbH
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

package org.dmfs.tasks.actions;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.homescreen.TaskListWidgetProvider;
import org.dmfs.tasks.homescreen.TaskListWidgetProviderLarge;
import org.dmfs.tasks.R;


/**
 * A {@link TaskAction} that updates the widgets.
 *
 * @author Trogel
 */
public final class UpdateWidgetsAction implements TaskAction
{
    public UpdateWidgetsAction()
    {
    }


    @Override
    public void execute(Context context, ContentProviderClient contentProviderClient, RowDataSnapshot<TaskContract.Instances> rowSnapshot, Uri taskUri)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        notifyTaskListDataChanged(appWidgetManager, new ComponentName(context, TaskListWidgetProvider.class));
        notifyTaskListDataChanged(appWidgetManager, new ComponentName(context, TaskListWidgetProviderLarge.class));
    }


    private void notifyTaskListDataChanged(AppWidgetManager appWidgetManager, ComponentName componentName)
    {
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.task_list_widget_lv);
    }
}
