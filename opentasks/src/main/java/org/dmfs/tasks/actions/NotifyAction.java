/*
 * Copyright 2019 dmfs GmbH
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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.text.format.DateUtils;

import org.dmfs.android.bolts.color.colors.AttributeColor;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.jems.function.Function;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.decorators.Sieved;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.opentaskspal.readdata.EffectiveDueDate;
import org.dmfs.opentaskspal.readdata.TaskCompletionTime;
import org.dmfs.opentaskspal.readdata.TaskDateTime;
import org.dmfs.opentaskspal.readdata.TaskIsClosed;
import org.dmfs.opentaskspal.readdata.TaskPin;
import org.dmfs.opentaskspal.readdata.TaskStart;
import org.dmfs.opentaskspal.readdata.TaskTitle;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.R;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.notification.ActionReceiver;
import org.dmfs.tasks.notification.ActionService;
import org.dmfs.tasks.notification.signals.Conditional;
import org.dmfs.tasks.utils.DateFormatter;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


/**
 * Post a notification.
 * <p>
 * TODO: refactor the notification construction, use composition
 *
 * @author Marten Gajda
 */
public final class NotifyAction implements TaskAction
{
    private final String GROUP_ALERTS = "org.dmfs.tasks.ALERTS";
    private final String GROUP_PINS = "org.dmfs.tasks.PINS";

    private final Function<RowDataSnapshot<? extends TaskContract.TaskColumns>, String> mChannelFunction;
    private final boolean mRepost;


    public NotifyAction(Function<RowDataSnapshot<? extends TaskContract.TaskColumns>, String> channelFunction, boolean repost)
    {
        mChannelFunction = channelFunction;
        mRepost = repost;
    }


    private static void createChannels(Context context)
    {
        if (Build.VERSION.SDK_INT >= 26)
        {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel pinnedChannel = new NotificationChannel(ActionService.CHANNEL_PINNED,
                    context.getString(R.string.opentasks_notification_channel_pinned_tasks),
                    NotificationManager.IMPORTANCE_DEFAULT);
            // pinned Notifications should not get a badge
            pinnedChannel.setShowBadge(true);
            pinnedChannel.enableLights(false);
            pinnedChannel.enableVibration(true);
            pinnedChannel.setVibrationPattern(new long[] { 0, 100, 100, 100, 0 });
            pinnedChannel.setSound(null, null);
            nm.createNotificationChannel(pinnedChannel);

            NotificationChannel dueDates = new NotificationChannel(ActionService.CHANNEL_DUE_DATES,
                    context.getString(R.string.opentasks_notification_channel_due_dates), NotificationManager.IMPORTANCE_HIGH);
            dueDates.setShowBadge(true);
            dueDates.enableLights(true);
            dueDates.enableVibration(true);
            nm.createNotificationChannel(dueDates);
        }
    }


    @Override
    public void execute(Context context, ContentProviderClient contentProviderClient, RowDataSnapshot<TaskContract.Instances> data, Uri taskUri) throws RemoteException, OperationApplicationException
    {
        // TODO: move to central place, for now we keep it here to be sure we have created the channels
        createChannels(context);

        Optional<CharSequence> title = new TaskTitle(data);
        boolean pin = new TaskPin(data).value();
        int notificationId = (int) ContentUris.parseId(taskUri);

        // build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, mChannelFunction.value(data))
                .setSmallIcon(pin ? R.drawable.ic_pin_white_24dp : R.drawable.ic_notification)
                .setContentTitle(new Backed<>(title, "Untitled Task").value())
                .setContentText(contentText(context, data));
        if (mRepost)
        {
            builder.setTicker(new Backed<>(title, "Untitled Task").value());
        }
        builder.setAutoCancel(false);
        builder.setContentIntent(PendingIntent.getBroadcast(context, notificationId,
                new Intent(context, ActionReceiver.class).setAction(
                        pin ? ActionService.ACTION_OPEN_TASK : ActionService.ACTION_OPEN_TASK_CANCEL_NOTIFICATION)
                        .setData(taskUri),
                PendingIntent.FLAG_UPDATE_CURRENT));

        // make sure we un-persist the notification when its cancelled
        builder.setDeleteIntent(PendingIntent.getBroadcast(context, notificationId,
                new Intent(context, ActionReceiver.class).setAction(ActionService.ACTION_REMOVE_NOTIFICATION).setData(taskUri),
                PendingIntent.FLAG_UPDATE_CURRENT));

        if (!new TaskIsClosed(data).value())
        {
            builder.addAction(
                    new NotificationCompat.Action(
                            R.drawable.ic_action_complete,
                            context.getString(R.string.notification_action_complete),
                            PendingIntent.getBroadcast(
                                    context,
                                    1,
                                    new Intent(context, ActionReceiver.class).setAction(ActionService.ACTION_COMPLETE).setData(taskUri),
                                    PendingIntent.FLAG_UPDATE_CURRENT)));

            if (new TaskDateTime(TaskContract.Tasks.DUE, data).isPresent())
            {
                builder.addAction(new NotificationCompat.Action(R.drawable.ic_detail_delay_1d_inverse, context.getString(R.string.notification_action_delay_1d),
                        PendingIntent.getBroadcast(
                                context,
                                1,
                                new Intent(context, ActionReceiver.class).setAction(ActionService.ACTION_DEFER_1D).setData(taskUri),
                                PendingIntent.FLAG_UPDATE_CURRENT)));
            }
        }

        if (pin)
        {
            builder.addAction(new NotificationCompat.Action(
                    R.drawable.ic_pin_off_white_24dp,
                    context.getString(R.string.notification_action_unpin),
                    PendingIntent.getBroadcast(
                            context,
                            1,
                            new Intent(context, ActionReceiver.class).setAction(ActionService.ACTION_UNPIN).setData(taskUri),
                            PendingIntent.FLAG_UPDATE_CURRENT)));
        }
        builder.setOnlyAlertOnce(!mRepost);
        builder.setOngoing(pin);
        builder.setShowWhen(false);
        builder.setGroup(pin ? GROUP_PINS : GROUP_ALERTS);
        builder.setPriority(pin ? NotificationCompat.PRIORITY_DEFAULT : NotificationCompat.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT < 26)
        {
            builder.setDefaults(new Conditional(mRepost, context).value());
        }
        // TODO: for now we only use the primary app color, later we allow the user to select how to color notifications: default, list, priority
        builder.setColor(new AttributeColor(new ContextThemeWrapper(context, R.style.OpenTasks_Theme_Default), R.attr.colorPrimary).argb());
        //builder.setColor(new EffectiveTaskColor(data).argb());
        NotificationManagerCompat.from(context).notify("tasks", notificationId, builder.build());
    }


    private CharSequence contentText(Context context, RowDataSnapshot<TaskContract.Instances> data)
    {
        Optional<DateTime> start = new TaskStart(data);
        Optional<DateTime> due = new EffectiveDueDate(data);
        if (new TaskCompletionTime(data).isPresent())
        {
            // TODO include completed time in notification text
            return context.getString(R.string.task_completed);
        }
        else if (due.isPresent() && (!start.isPresent() || new Sieved<>(DateTime.now()::after, start).isPresent()))
        {
            return context.getString(R.string.notification_task_due_date, formatTime(context, due.value()));
        }
        else if (start.isPresent())
        {
            return context.getString(R.string.notification_task_start_date, formatTime(context, start.value()));
        }
        return "";
    }


    /**
     * Returns a string representation for the time, with a relative date and an absolute time
     */
    public static String formatTime(Context context, DateTime time)
    {
        String dateString;
        if (time.isAllDay())
        {
            dateString = DateUtils.getRelativeTimeSpanString(time.getTimestamp(), DateTime.today().getTimestamp(), DateUtils.DAY_IN_MILLIS).toString();
        }
        else
        {
            dateString = DateUtils.getRelativeTimeSpanString(time.getTimestamp(), DateTime.now().getTimestamp(), DateUtils.DAY_IN_MILLIS).toString();
        }

        // return combined date and time
        String timeString = new DateFormatter(context).format(time, DateFormatter.DateFormatContext.NOTIFICATION_VIEW_TIME);
        return dateString + ", " + timeString;
    }

}
