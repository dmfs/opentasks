/*
 * Copyright 2018 dmfs GmbH
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

package org.dmfs.opentaskstestpal;

import android.content.ContentProviderOperation;
import android.support.annotation.NonNull;

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.optional.Optional;
import org.dmfs.optional.Present;
import org.dmfs.optional.composite.Zipped;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;

import static org.dmfs.optional.Absent.absent;


/**
 * {@link RowData} of the instance table. This sets all values except for {@link TaskContract.Instances#TASK_ID}.
 * <p>
 * Note: this is meant for use with an assert operation during tests as the instances table is read only and doesn't allow inserts nor updates.
 *
 * @author Marten Gajda
 */
public final class InstanceTestData implements RowData<TaskContract.Instances>
{
    private final Optional<DateTime> mInstanceStart;
    private final Optional<DateTime> mInstanceDue;
    private final Optional<DateTime> mOriginalTime;
    private final int mDistanceFromCurrent;


    public InstanceTestData(int distanceFromCurrent)
    {
        this(absent(), absent(), absent(), distanceFromCurrent);
    }


    public InstanceTestData(@NonNull DateTime instanceStart, @NonNull DateTime instanceDue, @NonNull Optional<DateTime> originalTime, int distanceFromCurrent)
    {
        this(new Present<>(instanceStart), new Present<>(instanceDue), originalTime, distanceFromCurrent);
    }


    public InstanceTestData(@NonNull Optional<DateTime> instanceStart, @NonNull Optional<DateTime> instanceDue, @NonNull Optional<DateTime> originalTime, int distanceFromCurrent)
    {
        mInstanceStart = instanceStart;
        mInstanceDue = instanceDue;
        mOriginalTime = originalTime;
        mDistanceFromCurrent = distanceFromCurrent;
    }


    @NonNull
    @Override
    public ContentProviderOperation.Builder updatedBuilder(@NonNull TransactionContext transactionContext, @NonNull ContentProviderOperation.Builder builder)
    {
        return builder
                .withValue(TaskContract.Instances.INSTANCE_START, new Mapped<>(DateTime::getTimestamp, mInstanceStart).value(null))
                .withValue(TaskContract.Instances.INSTANCE_START_SORTING, new Mapped<>(DateTime::getInstance, mInstanceStart).value(null))
                .withValue(TaskContract.Instances.INSTANCE_DUE, new Mapped<>(DateTime::getTimestamp, mInstanceDue).value(null))
                .withValue(TaskContract.Instances.INSTANCE_DUE_SORTING, new Mapped<>(DateTime::getInstance, mInstanceDue).value(null))
                .withValue(TaskContract.Instances.INSTANCE_DURATION,
                        new Zipped<>(mInstanceStart, mInstanceDue, (start, due) -> (due.getTimestamp() - start.getTimestamp())).value(null))
                .withValue(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, mOriginalTime.value(new DateTime(0)).getTimestamp())
                .withValue(TaskContract.Instances.DISTANCE_FROM_CURRENT, mDistanceFromCurrent);
    }

}
