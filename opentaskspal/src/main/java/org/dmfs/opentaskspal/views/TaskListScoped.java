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

package org.dmfs.opentaskspal.views;

import android.database.Cursor;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import org.dmfs.android.contentpal.InsertOperation;
import org.dmfs.android.contentpal.Predicate;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.Table;
import org.dmfs.android.contentpal.UriParams;
import org.dmfs.android.contentpal.View;
import org.dmfs.opentaskspal.predicates.TaskOnList;
import org.dmfs.optional.Optional;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A view onto {@link TaskContract.Tasks} which contains only events from a specific task list.
 * `{@link InsertOperation}`s returned by the result of {@link #table()} will insert with the given task list by default.
 * <p>
 * Note, if you pass a virtual {@link RowSnapshot}, the {@link View} will always be empty, even after adding and committing rows.
 *
 * @author Gabor Keszthelyi
 */
public final class TaskListScoped implements View<TaskContract.Tasks>
{
    private final View<TaskContract.Tasks> mDelegate;
    private final RowSnapshot<TaskContract.TaskLists> mTaskListRow;


    public TaskListScoped(@NonNull RowSnapshot<TaskContract.TaskLists> taskListRow, @NonNull View<TaskContract.Tasks> delegate)
    {
        mDelegate = delegate;
        mTaskListRow = taskListRow;
    }


    @NonNull
    @Override
    public Cursor rows(@NonNull UriParams uriParams, @NonNull Predicate predicate, @NonNull Optional<String> sorting) throws RemoteException
    {
        // TODO See todo in TaskOnList
        return mDelegate.rows(uriParams, new TaskOnList(mTaskListRow, predicate), sorting);
    }


    @NonNull
    @Override
    public Table<TaskContract.Tasks> table()
    {
        return new org.dmfs.opentaskspal.tables.TaskListScoped(mTaskListRow, mDelegate.table());
    }
}
