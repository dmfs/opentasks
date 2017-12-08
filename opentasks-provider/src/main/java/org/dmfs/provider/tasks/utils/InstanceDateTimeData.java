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

package org.dmfs.provider.tasks.utils;

import android.content.ContentValues;

import org.dmfs.iterables.elementary.Seq;
import org.dmfs.jems.function.BiFunction;
import org.dmfs.jems.single.Single;
import org.dmfs.optional.Optional;
import org.dmfs.optional.adapters.FirstPresent;
import org.dmfs.optional.composite.Zipped;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.contract.TaskContract;

import java.util.TimeZone;


/**
 * A {@link Single} holding the date and time {@link ContentValues} of an instance.
 *
 * @author Marten Gajda
 */
public final class InstanceDateTimeData implements Single<ContentValues>
{
    private final Optional<DateTime> mOptionalStart;
    private final Optional<DateTime> mOptionalDue;
    private final Optional<Duration> mOptionalDuration;
    private final Optional<DateTime> mOriginalStart;


    public InstanceDateTimeData(Optional<DateTime> optionalStart, Optional<DateTime> optionalDue, Optional<Duration> optionalDuration, Optional<DateTime> originalStart)
    {
        mOptionalStart = optionalStart;
        mOptionalDue = optionalDue;
        mOptionalDuration = optionalDuration;
        mOriginalStart = originalStart;
    }


    @Override
    public ContentValues value()
    {
        ContentValues instanceValues = new ContentValues();
        TimeZone localTz = TimeZone.getDefault();

        Optional<DateTime> effectiveDue = new FirstPresent<>(
                new Seq<>(mOptionalDue, new Zipped<>(mOptionalStart, mOptionalDuration, new AddDurationBiFunction())));
        Optional<Long> effectiveDuration = new Zipped<>(mOptionalStart, effectiveDue, new DurationBiFunction());

        putDateValue(instanceValues, mOptionalStart, TaskContract.Instances.INSTANCE_START, TaskContract.Instances.INSTANCE_START_SORTING, localTz);
        putDateValue(instanceValues, effectiveDue, TaskContract.Instances.INSTANCE_DUE, TaskContract.Instances.INSTANCE_DUE_SORTING, localTz);
        instanceValues.put(TaskContract.Instances.INSTANCE_DURATION, effectiveDuration.value(null));
        instanceValues.put(TaskContract.Instances.INSTANCE_ORIGINAL_TIME,
                new FirstPresent<>(new Seq<>(mOriginalStart, mOptionalStart, effectiveDue)).value(new DateTime(0)).getTimestamp());

        return instanceValues;
    }


    private void putDateValue(ContentValues values, Optional<DateTime> value, String timestampColumn, String sortingColumn, TimeZone localTimeZone)
    {
        if (value.isPresent())
        {
            // add timestamp and sorting
            DateTime dateTime = value.value();
            values.put(timestampColumn, dateTime.getTimestamp());
            values.put(sortingColumn, dateTime.isAllDay() ? dateTime.getInstance() : dateTime.shiftTimeZone(localTimeZone).getInstance());
        }
        else
        {
            values.putNull(timestampColumn);
            values.putNull(sortingColumn);
        }
    }


    private static final class AddDurationBiFunction implements BiFunction<DateTime, Duration, DateTime>
    {
        @Override
        public DateTime value(DateTime dateTime, Duration duration)
        {
            return dateTime.addDuration(duration);
        }
    }


    private static final class DurationBiFunction implements BiFunction<DateTime, DateTime, Long>
    {
        @Override
        public Long value(DateTime startDateTime, DateTime dueDateTime)
        {
            return dueDateTime.getTimestamp() - startDateTime.getTimestamp();
        }
    }
}
