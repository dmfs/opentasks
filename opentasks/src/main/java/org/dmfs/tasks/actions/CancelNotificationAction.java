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

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import androidx.core.app.NotificationManagerCompat;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A {@link TaskAction} which cancels a notification.
 *
 * @author Marten Gajda
 */
public final class CancelNotificationAction implements TaskAction
{

    private final String mNotificationTag;


    public CancelNotificationAction()
    {
        this("tasks");
    }


    public CancelNotificationAction(String notificationTag)
    {
        mNotificationTag = notificationTag;
    }


    @Override
    public void execute(Context context, ContentProviderClient contentProviderClient, RowDataSnapshot<TaskContract.Instances> rowSnapshot, Uri taskUri)
    {
        NotificationManagerCompat.from(context).cancel(mNotificationTag, (int) ContentUris.parseId(taskUri));
    }
}
