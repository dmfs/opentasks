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

package org.dmfs.tasks.notification;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import org.dmfs.tasks.R;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A {@link BroadcastReceiver} to handle incoming alarms for tasks.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver
{

    /**
     * Is called on an incoming alarm broadcast. Creates a notifications for this alarm.
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // continue if alarms where enabled
        if (intent.getAction().equals(TaskContract.ACTION_BROADCAST_TASK_STARTING))
        {
            if (isNotificationEnabled(context))
            {
                NotificationUpdaterService.createChannels(context);
                Uri taskUri = intent.getData();

                boolean noSignal = intent.getBooleanExtra(NotificationActionUtils.EXTRA_NOTIFICATION_NO_SIGNAL, false);
                // check for pinned task
                if (TaskNotificationHandler.isTaskPinned(context, taskUri))
                {
                    TaskNotificationHandler.sendPinnedTaskStartNotification(context, taskUri, noSignal);
                    return;
                }

                // create regular notification
                String title = intent.getStringExtra(TaskContract.EXTRA_TASK_TITLE);
                long startDate = intent.getLongExtra(TaskContract.EXTRA_TASK_TIMESTAMP, Long.MIN_VALUE);
                boolean startAllDay = intent.getBooleanExtra(TaskContract.EXTRA_TASK_ALLDAY, false);
                String timezone = intent.getStringExtra(TaskContract.EXTRA_TASK_TIMEZONE);
                int notificationId = (int) ContentUris.parseId(taskUri);

                NotificationActionUtils.sendStartNotification(context, title, taskUri, notificationId, startDate, startAllDay, timezone, noSignal);

            }
        }
        else if (intent.getAction().equals(TaskContract.ACTION_BROADCAST_TASK_DUE))
        {
            if (isNotificationEnabled(context))
            {
                NotificationUpdaterService.createChannels(context);
                Uri taskUri = intent.getData();

                boolean noSignal = intent.getBooleanExtra(NotificationActionUtils.EXTRA_NOTIFICATION_NO_SIGNAL, false);

                // check for pinned task
                if (TaskNotificationHandler.isTaskPinned(context, taskUri))
                {
                    TaskNotificationHandler.sendPinnedTaskDueNotification(context, taskUri, noSignal);
                    return;
                }

                // create regular notification
                // long dueTime = intent.getLongExtra(AlarmNotificationHandler.EXTRA_TASK_DUE_TIME, System.currentTimeMillis());
                String title = intent.getStringExtra(TaskContract.EXTRA_TASK_TITLE);
                long dueDate = intent.getLongExtra(TaskContract.EXTRA_TASK_TIMESTAMP, Long.MIN_VALUE);
                boolean dueAllDay = intent.getBooleanExtra(TaskContract.EXTRA_TASK_ALLDAY, false);
                String timezone = intent.getStringExtra(TaskContract.EXTRA_TASK_TIMEZONE);
                int notificationId = (int) ContentUris.parseId(taskUri);

                NotificationActionUtils.sendDueAlarmNotification(context, title, taskUri, notificationId, dueDate, dueAllDay, timezone, noSignal);
            }
        }

    }


    public boolean isNotificationEnabled(Context context)
    {
        if (Build.VERSION.SDK_INT >= 26)
        {
            // on Android 8+ we leave this decision to Android and always attempt to show the notification
            return true;
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(context.getString(R.string.opentasks_pref_notification_enabled), true);

    }
}
