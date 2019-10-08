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

package org.dmfs.opentaskspal.tables;

import android.content.ContentProviderClient;

import org.dmfs.android.contentpal.InsertOperation;
import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.Predicate;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.Table;
import org.dmfs.android.contentpal.UriParams;
import org.dmfs.android.contentpal.View;
import org.dmfs.opentaskspal.operations.TaskListTask;
import org.dmfs.opentaskspal.predicates.TaskOnList;
import org.dmfs.tasks.contract.TaskContract;

import androidx.annotation.NonNull;


/**
 * A view onto the {@link TaskContract.Tasks} table which contains only tasks from a specific task list.
 * Tasks created with {@link #insertOperation(UriParams)} will automatically be added to this task list.
 *
 * @author Marten Gajda
 * @author Gabor Keszthelyi
 */
public final class TaskListScoped implements Table<TaskContract.Tasks>
{
    private final Table<TaskContract.Tasks> mDelegate;
    private final RowSnapshot<TaskContract.TaskLists> mTaskListRow;


    public TaskListScoped(@NonNull RowSnapshot<TaskContract.TaskLists> taskListRow, @NonNull Table<TaskContract.Tasks> delegate)
    {
        mDelegate = delegate;
        mTaskListRow = taskListRow;
    }


    @NonNull
    @Override
    public InsertOperation<TaskContract.Tasks> insertOperation(@NonNull UriParams uriParams)
    {
        return new TaskListTask(mTaskListRow, mDelegate.insertOperation(uriParams));
    }


    @NonNull
    @Override
    public Operation<TaskContract.Tasks> updateOperation(@NonNull UriParams uriParams, @NonNull Predicate<? super TaskContract.Tasks> predicate)
    {
        return mDelegate.updateOperation(uriParams, new TaskOnList(mTaskListRow, predicate));
    }


    @NonNull
    @Override
    public Operation<TaskContract.Tasks> deleteOperation(@NonNull UriParams uriParams, @NonNull Predicate<? super TaskContract.Tasks> predicate)
    {
        return mDelegate.deleteOperation(uriParams, new TaskOnList(mTaskListRow, predicate));
    }


    @NonNull
    @Override
    public Operation<TaskContract.Tasks> assertOperation(@NonNull UriParams uriParams, @NonNull Predicate<? super TaskContract.Tasks> predicate)
    {
        return mDelegate.assertOperation(uriParams, new TaskOnList(mTaskListRow, predicate));
    }


    public View<TaskContract.Tasks> view(@NonNull ContentProviderClient client)
    {
        return new org.dmfs.opentaskspal.views.TaskListScoped(mTaskListRow, mDelegate.view(client));
    }

}
