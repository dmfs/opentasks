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
import org.dmfs.tasks.utils.WidgetConfigurationDatabaseHelper;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


/**
 * The provider for the full 4x4 widget for pre Honeycomb devices.
 * Inherits the same functionality from the small widget.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TaskListWidgetProviderLargeLegacy extends TaskListWidgetProviderLegacy
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
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		super.onDeleted(context, appWidgetIds);
	}


	@Override
	protected ComponentName getComponentName(Context context)
	{
		return new ComponentName(context, TaskListWidgetProviderLargeLegacy.class);
	}
}
