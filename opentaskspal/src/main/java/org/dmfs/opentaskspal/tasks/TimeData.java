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

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.elementary.Absent;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.contract.TaskContract;

import androidx.annotation.NonNull;


/**
 * {@link RowData} for adding start, start+due, or start+duration times.
 *
 * @author Gabor Keszthelyi
 */
public final class TimeData<T extends TaskContract.TaskColumns> implements RowData<T>
{
    private final DateTime mStart;
    private final Optional<DateTime> mDue;
    private final Optional<Duration> mDuration;


    private TimeData(@NonNull DateTime start, @NonNull Optional<DateTime> due, @NonNull Optional<Duration> duration)
    {
        mStart = start;
        mDue = due;
        mDuration = duration;
    }


    public TimeData(@NonNull DateTime start, @NonNull DateTime due)
    {
        this(start, new Present<>(due), Absent.absent());
    }


    public TimeData(@NonNull DateTime start, @NonNull Duration duration)
    {
        this(start, Absent.absent(), new Present<>(duration));
    }


    public TimeData(@NonNull DateTime start)
    {
        this(start, Absent.absent(), Absent.absent());
    }


    @NonNull
    @Override
    public ContentProviderOperation.Builder updatedBuilder(@NonNull TransactionContext transactionContext, @NonNull ContentProviderOperation.Builder builder)
    {
        if (mDue.isPresent() && mStart.isAllDay() != mDue.value().isAllDay())
        {
            throw new IllegalArgumentException("'start' and 'due' must have the same all-day flag");
        }

        DateTime start = mStart;
        if (mDue.isPresent() && !mDue.value().isAllDay())
        {
            start = mStart.shiftTimeZone(mDue.value().getTimeZone());
        }

        return doUpdateBuilder(start, mDue, mDuration, builder);
    }


    private static ContentProviderOperation.Builder doUpdateBuilder(DateTime start,
                                                                    Optional<DateTime> due,
                                                                    Optional<Duration> duration,
                                                                    ContentProviderOperation.Builder builder)
    {
        return builder
                .withValue(TaskContract.Tasks.DTSTART, start.getTimestamp())
                .withValue(TaskContract.Tasks.TZ, start.isAllDay() ? "UTC" : start.getTimeZone().getID())
                .withValue(TaskContract.Tasks.IS_ALLDAY, start.isAllDay() ? 1 : 0)
                .withValue(TaskContract.Tasks.DUE, due.isPresent() ? due.value().getTimestamp() : null)
                .withValue(TaskContract.Tasks.DURATION, duration.isPresent() ? duration.value().toString() : null);
    }
}
