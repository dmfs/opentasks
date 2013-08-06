/*
 * 
 *
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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

import android.content.Context;
import android.content.Intent;


/**
 * The utilites used by the HomeScreen widget.
 */
public class WidgetUtils
{

	/**
	 * Gets the widget update action string.
	 * 
	 * @param context
	 *            the context
	 * @return the update action
	 */
	public static String getUpdateAction(Context context)
	{
		return context.getPackageName() + ".action.TASKWIDGET_UPDATE";
	}


	/**
	 * Broadcast widget update.
	 * 
	 * @param context
	 *            context of the app.
	 */
	public static void broadcastWidgetUpdate(Context context)
	{
		Intent updateWidgetBroadcast = new Intent();
		String actionString = WidgetUtils.getUpdateAction(context);
		updateWidgetBroadcast.setAction(actionString);
		context.sendBroadcast(updateWidgetBroadcast);
	}
}
