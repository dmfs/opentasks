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
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import androidx.core.app.TaskStackBuilder;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A {@link TaskAction} which opens a task.
 *
 * @author Marten Gajda
 */
public final class OpenAction implements TaskAction
{
    @Override
    public void execute(Context context, ContentProviderClient contentProviderClient, RowDataSnapshot<TaskContract.Instances> rowSnapshot, Uri taskUri) throws RemoteException, OperationApplicationException
    {
        // TODO: check if it's still a good idea to build a custom stack
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
        resultIntent.setData(taskUri);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        context.startActivities(stackBuilder.getIntents());
    }
}
