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

import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.widget.RemoteViews;

import org.dmfs.android.bolts.color.colors.AttributeColor;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.tasks.R;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.notification.ActionReceiver;
import org.dmfs.tasks.notification.ActionService;
import org.dmfs.tasks.notification.signals.NoSignal;

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
public final class PostUndoAction implements TaskAction
{
    private final String GROUP_UNDO = "org.dmfs.tasks.UNDO";


    @Override
    public void execute(Context context, ContentProviderClient contentProviderClient, RowDataSnapshot<TaskContract.Instances> data, Uri taskUri) throws RemoteException, OperationApplicationException
    {
        int id = (int) ContentUris.parseId(taskUri);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ActionService.CHANNEL_DUE_DATES);
        builder.setContentTitle(context.getString(R.string.task_completed));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        builder.setDefaults(new NoSignal().value());

        final RemoteViews undoView = new RemoteViews(context.getPackageName(), R.layout.undo_notification);
        undoView.setTextViewText(R.id.description_text, context.getString(R.string.task_completed));

        undoView.setOnClickPendingIntent(
                R.id.status_bar_latest_event_content,
                PendingIntent.getBroadcast(
                        context,
                        id,
                        new Intent(context, ActionReceiver.class).setData(taskUri).setAction(ActionService.ACTION_UNDO_COMPLETE),
                        PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContent(undoView);

        // When the notification is cleared, we perform the destructive action
        builder.setDeleteIntent(PendingIntent.getBroadcast(
                context,
                id,
                new Intent(context, ActionReceiver.class).setData(taskUri).setAction(ActionService.ACTION_FINISH_COMPLETE),
                PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setShowWhen(false);
        builder.setGroup(GROUP_UNDO);
        builder.setColor(new AttributeColor(new ContextThemeWrapper(context, R.style.OpenTasks_Theme_Default), R.attr.colorPrimary).argb());

        NotificationManagerCompat.from(context).notify("tasks.undo", id, builder.build());
    }

}
