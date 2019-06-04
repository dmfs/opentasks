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

package org.dmfs.opentaskspal.operations;

import android.content.ContentProviderOperation;
import androidx.annotation.NonNull;

import org.dmfs.android.contentpal.InsertOperation;
import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.SoftRowReference;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.android.contentpal.operations.Referring;
import org.dmfs.jems.optional.Optional;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link InsertOperation} decorator which relates a new task to a task list.
 *
 * @author Gabor Keszthelyi
 */
public final class TaskListTask implements InsertOperation<TaskContract.Tasks>
{
    private final Operation<TaskContract.Tasks> mDelegate;


    public TaskListTask(@NonNull RowSnapshot<TaskContract.TaskLists> taskList, @NonNull InsertOperation<TaskContract.Tasks> original)
    {
        mDelegate = new Referring<>(taskList, TaskContract.Tasks.LIST_ID, original);
    }


    @NonNull
    @Override
    public Optional<SoftRowReference<TaskContract.Tasks>> reference()
    {
        return mDelegate.reference();
    }


    @NonNull
    @Override
    public ContentProviderOperation.Builder contentOperationBuilder(@NonNull TransactionContext transactionContext) throws UnsupportedOperationException
    {
        return mDelegate.contentOperationBuilder(transactionContext);
    }
}
