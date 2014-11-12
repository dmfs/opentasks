/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
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

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;


/**
 * Listens for the ProviderChanged broadcast in order to update the app widgets.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class ProviderChangedBroadcastReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// update all widgets
		ComponentName smallProvider = new ComponentName(context, TaskListWidgetProvider.class);
		int[] smallWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(smallProvider);

		ComponentName largeProvider = new ComponentName(context, TaskListWidgetProviderLarge.class);
		int[] largeWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(largeProvider);

		/** Create an Intent with the {@link RemoteViewsService } and pass it the Widget Id */
		Intent remoteServiceIntent = new Intent(context, TaskListWidgetUpdaterService.class);
		remoteServiceIntent.setData(Uri.parse(remoteServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));

		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		new TaskListWidgetProviderLarge().onUpdate(context, manager, largeWidgetIds);
		new TaskListWidgetProvider().onUpdate(context, manager, smallWidgetIds);

		for (int id : smallWidgetIds)
		{
			remoteServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
			context.startService(remoteServiceIntent);
		}

		for (int id : largeWidgetIds)
		{
			remoteServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
			context.startService(remoteServiceIntent);
		}
	}
}
