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

package org.dmfs.opentaskspal.tasks;

import android.content.ContentProviderOperation;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link RowData} for adding start, start+due, or start+duration times.
 *
 * @author Gabor Keszthelyi
 */
public final class TimeData implements RowData<TaskContract.Tasks>
{
    private final Long mStartTimeStamp;
    private final Long mDueTimeStamp;
    private final String mDuration;


    private TimeData(@NonNull Long startTimeStamp, @Nullable Long dueTimeStamp, @Nullable String duration)
    {
        mStartTimeStamp = startTimeStamp;
        mDueTimeStamp = dueTimeStamp;
        mDuration = duration;
    }


    public TimeData(@NonNull Long startTimeStamp, @NonNull Long dueTimeStamp)
    {
        this(startTimeStamp, dueTimeStamp, null);
    }


    public TimeData(@NonNull Long startTimeStamp, @NonNull String duration)
    {
        this(startTimeStamp, null, duration);
    }


    public TimeData(@NonNull Long startTimeStamp)
    {
        this(startTimeStamp, null, null);
    }


    @NonNull
    @Override
    public ContentProviderOperation.Builder updatedBuilder(@NonNull TransactionContext transactionContext, @NonNull ContentProviderOperation.Builder builder)
    {
        return builder
                .withValue(TaskContract.Tasks.DTSTART, mStartTimeStamp)
                .withValue(TaskContract.Tasks.DUE, mDueTimeStamp)
                .withValue(TaskContract.Tasks.DURATION, mDuration);
    }
}
