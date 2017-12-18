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
import org.dmfs.iterators.SingletonIterator;
import org.dmfs.jems.function.BiFunction;
import org.dmfs.jems.single.Single;
import org.dmfs.optional.NullSafe;
import org.dmfs.optional.Optional;
import org.dmfs.optional.adapters.FirstPresent;
import org.dmfs.optional.composite.Zipped;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;

import java.util.Iterator;


/**
 * An {@link Iterable} of {@link Single} {@link ContentValues} of the instances of a task.
 *
 * @author Marten Gajda
 */
public final class InstanceValuesIterable implements Iterable<Single<ContentValues>>
{
    private final TaskAdapter mTaskAdapter;


    public InstanceValuesIterable(TaskAdapter taskAdapter)
    {
        mTaskAdapter = taskAdapter;
    }


    @Override
    public Iterator<Single<ContentValues>> iterator()
    {
        Optional<DateTime> start = new NullSafe<>(mTaskAdapter.valueOf(TaskAdapter.DTSTART));
        Optional<DateTime> due = new NullSafe<>(mTaskAdapter.valueOf(TaskAdapter.DUE));

        Optional<Duration> effectiveDuration = new FirstPresent<>(
                new Seq<>(
                        new NullSafe<>(mTaskAdapter.valueOf(TaskAdapter.DURATION)),
                        new Zipped<>(start, due, new DateTimeDurationBiFunction())));

        // TODO: implement support for recurrence, for now we only return the first instance
        return new SingletonIterator<Single<ContentValues>>(
                new InstanceDateTimeData(start, due, effectiveDuration, new NullSafe<>(mTaskAdapter.valueOf(TaskAdapter.ORIGINAL_INSTANCE_TIME))));
    }


    /**
     * A {@link BiFunction} returning the duration between two {@link DateTime} values.
     */
    private final static class DateTimeDurationBiFunction implements BiFunction<DateTime, DateTime, Duration>
    {
        @Override
        public Duration value(DateTime start, DateTime due)
        {
            return new Duration(1, 0, (int) ((due.getTimestamp() - start.getTimestamp()) / 1000));
        }
    }
}
