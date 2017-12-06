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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


/**
 * The provider for the large homescreen widget on Android Honeycomb and up.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
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
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
    }


    @Override
    protected ComponentName getComponentName(Context context)
    {
        return new ComponentName(context, TaskListWidgetProviderLarge.class);
    }
}
