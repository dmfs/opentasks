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

import org.dmfs.tasks.R;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


/**
 * The Class TaskListWidgetProvider.
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TaskListWidgetProviderLarge extends TaskListWidgetProvider
{

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
		return new ComponentName(context, TaskListWidgetProviderLarge.class);
	}
}
