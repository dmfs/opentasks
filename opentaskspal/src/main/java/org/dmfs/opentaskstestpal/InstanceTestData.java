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

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.composite.Zipped;
import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;

import java.util.TimeZone;

import androidx.annotation.NonNull;

import static org.dmfs.jems.optional.elementary.Absent.absent;


/**
 * {@link RowData} of the instance view. This sets all instance values except for {@link TaskContract.Instances#TASK_ID} as well as some instance specific task
 * values.
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
                .withValue(TaskContract.Instances.INSTANCE_START_SORTING, new Mapped<>(this::toInstance, mInstanceStart).value(null))
                .withValue(TaskContract.Instances.INSTANCE_DUE, new Mapped<>(DateTime::getTimestamp, mInstanceDue).value(null))
                .withValue(TaskContract.Instances.INSTANCE_DUE_SORTING, new Mapped<>(this::toInstance, mInstanceDue).value(null))
                .withValue(TaskContract.Instances.INSTANCE_DURATION,
                        new Backed<>(
                                new Zipped<>(
                                        mInstanceStart,
                                        mInstanceDue,
                                        (start, due) -> (due.getTimestamp() - start.getTimestamp())),
                                () -> null)
                                .value())
                .withValue(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, new Mapped<>(DateTime::getTimestamp, mOriginalTime).value(null))
                .withValue(TaskContract.Instances.DISTANCE_FROM_CURRENT, mDistanceFromCurrent)
                // the instances view overrides some of the task values. Since they are closely tied to the instance data we test them here as well.
                .withValue(TaskContract.Instances.DTSTART, new Mapped<>(DateTime::getTimestamp, mInstanceStart).value(null))
                .withValue(TaskContract.Instances.DUE, new Mapped<>(DateTime::getTimestamp, mInstanceDue).value(null))
                .withValue(TaskContract.Instances.ORIGINAL_INSTANCE_TIME, new Mapped<>(DateTime::getTimestamp, mOriginalTime).value(null))
                .withValue(TaskContract.Instances.DURATION, null)
                .withValue(TaskContract.Instances.RRULE, null)
                .withValue(TaskContract.Instances.RDATE, null)
                .withValue(TaskContract.Instances.EXDATE, null);
    }


    private long toInstance(DateTime dateTime)
    {
        return (dateTime.isAllDay() ? dateTime : dateTime.shiftTimeZone(TimeZone.getDefault())).getInstance();
    }

}
