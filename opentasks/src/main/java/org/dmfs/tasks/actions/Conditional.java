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
import org.dmfs.jems.function.BiFunction;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A {@link TaskAction} which is only executed if a test function returns {@code true}.
 *
 * @author Marten Gajda
 */
public final class Conditional implements TaskAction
{
    private final BiFunction<Context, RowDataSnapshot<? extends TaskContract.TaskColumns>, Boolean> mTestFunction;
    private final TaskAction mDelegate;


    public Conditional(BiFunction<Context, RowDataSnapshot<? extends TaskContract.TaskColumns>, Boolean> testFunction, TaskAction delegate)
    {
        mTestFunction = testFunction;
        mDelegate = delegate;
    }


    @Override
    public void execute(Context context, ContentProviderClient contentProviderClient, RowDataSnapshot<TaskContract.Instances> data, Uri taskUri) throws RemoteException, OperationApplicationException
    {
        if (mTestFunction.value(context, data))
        {
            mDelegate.execute(context, contentProviderClient, data, taskUri);
        }
    }
}
