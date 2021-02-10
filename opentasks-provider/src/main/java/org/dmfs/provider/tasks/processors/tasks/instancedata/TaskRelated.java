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

package org.dmfs.provider.tasks.processors.tasks.instancedata;

import android.content.ContentValues;

import org.dmfs.jems.single.Single;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A decorator to {@link Single}s of {@link ContentValues} adding a {@link TaskContract.Instances#TASK_ID} to the data.
 *
 * @author Marten Gajda
 */
public final class TaskRelated implements Single<ContentValues>
{
    private final long mTaskId;
    private final Single<ContentValues> mDelegate;


    public TaskRelated(TaskAdapter taskAdapter, Single<ContentValues> delegate)
    {
        this(taskAdapter.id(), delegate);
    }


    public TaskRelated(long taskId, Single<ContentValues> delegate)
    {
        mTaskId = taskId;
        mDelegate = delegate;
    }


    @Override
    public ContentValues value()
    {
        ContentValues values = mDelegate.value();
        values.put(TaskContract.Instances.TASK_ID, mTaskId);
        return values;
    }
}
