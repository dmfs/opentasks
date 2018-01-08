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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import androidx.core.app.AlarmManagerCompat;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.notification.ActionReceiver;


/**
 * A {@link TaskAction} which executes another action after a given time.
 *
 * @author Marten Gajda
 */
public final class DelayedAction implements TaskAction
{
    private final String mAction;
    private final int mDelayMillis;


    public DelayedAction(String action, int delayMillis)
    {
        mAction = action;
        mDelayMillis = delayMillis;
    }


    @Override
    public void execute(Context context, ContentProviderClient contentProviderClient, RowDataSnapshot<TaskContract.Instances> rowSnapshot, Uri taskUri) throws RemoteException, OperationApplicationException
    {
        AlarmManagerCompat.setExactAndAllowWhileIdle(
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE),
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + mDelayMillis,
                PendingIntent.getBroadcast(
                        context,
                        (int) ContentUris.parseId(taskUri),
                        new Intent(context, ActionReceiver.class).setAction(mAction).setData(taskUri),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
