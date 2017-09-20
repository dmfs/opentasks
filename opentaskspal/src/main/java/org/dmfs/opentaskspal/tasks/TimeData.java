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
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link RowData} for adding start, start+due, or start+duration times.
 *
 * @author Gabor Keszthelyi
 */
// TODO Unit test
public final class TimeData implements RowData<TaskContract.Tasks>
{
    @NonNull
    private final DateTime mStart;
    @Nullable
    private final DateTime mDue;
    @Nullable
    private final Duration mDuration;


    private TimeData(@NonNull DateTime start, @Nullable DateTime due, @Nullable Duration duration)
    {
        mStart = start;
        mDue = due;
        mDuration = duration;
    }


    public TimeData(@NonNull DateTime start, @NonNull DateTime due)
    {
        this(start, due, null);
    }


    public TimeData(@NonNull DateTime start, @NonNull Duration duration)
    {
        this(start, null, duration);
    }


    public TimeData(@NonNull DateTime start)
    {
        this(start, null, null);
    }


    @NonNull
    @Override
    public ContentProviderOperation.Builder updatedBuilder(@NonNull TransactionContext transactionContext, @NonNull ContentProviderOperation.Builder builder)
    {
        if (mDue != null && mStart.isAllDay() != mDue.isAllDay())
        {
            throw new IllegalArgumentException("'start' and 'due' must have the same all-day flag");
        }

        DateTime start = mStart;
        if (mDue != null && !mDue.isAllDay())
        {
            start = mStart.shiftTimeZone(mDue.getTimeZone());
        }

        return doUpdateBuilder(start, mDue, mDuration, builder);
    }


    private static ContentProviderOperation.Builder doUpdateBuilder(@NonNull DateTime start,
                                                                    @Nullable DateTime due,
                                                                    @Nullable Duration duration,
                                                                    ContentProviderOperation.Builder builder)
    {
        return builder
                .withValue(TaskContract.Tasks.DTSTART, start.getTimestamp())
                .withValue(TaskContract.Tasks.TZ, start.isAllDay() ? "UTC" : start.getTimeZone().getID())
                .withValue(TaskContract.Tasks.IS_ALLDAY, start.isAllDay() ? 1 : 0)

                .withValue(TaskContract.Tasks.DUE, due == null ? null : due.getTimestamp())

                .withValue(TaskContract.Tasks.DURATION, duration == null ? null : duration.toString())

                .withValue(TaskContract.Tasks.RDATE, null)
                .withValue(TaskContract.Tasks.RRULE, null)
                .withValue(TaskContract.Tasks.EXDATE, null);
    }
}
