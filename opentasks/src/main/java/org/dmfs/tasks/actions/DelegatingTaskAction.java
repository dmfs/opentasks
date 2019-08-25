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
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.tasks.contract.TaskContract;


/**
 * An abstract {@link TaskAction} which only delegates to another {@link TaskAction}. This is meant to easy composition.
 *
 * @author Marten Gajda
 */
public abstract class DelegatingTaskAction implements TaskAction
{
    private final TaskAction mDelegate;


    protected DelegatingTaskAction(TaskAction delegate)
    {
        mDelegate = delegate;
    }


    public final void execute(Context context, ContentProviderClient contentProviderClient, RowDataSnapshot<TaskContract.Instances> rowSnapshot, Uri taskUri) throws RemoteException, OperationApplicationException
    {
        mDelegate.execute(context, contentProviderClient, rowSnapshot, taskUri);
    }
}
