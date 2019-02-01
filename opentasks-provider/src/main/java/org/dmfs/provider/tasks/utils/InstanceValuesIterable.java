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
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.adapters.FirstPresent;
import org.dmfs.jems.optional.composite.Zipped;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.single.Single;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.tasks.instancedata.Distant;
import org.dmfs.provider.tasks.processors.tasks.instancedata.DueDated;
import org.dmfs.provider.tasks.processors.tasks.instancedata.Enduring;
import org.dmfs.provider.tasks.processors.tasks.instancedata.Overridden;
import org.dmfs.provider.tasks.processors.tasks.instancedata.StartDated;
import org.dmfs.provider.tasks.processors.tasks.instancedata.VanillaInstanceData;
import org.dmfs.rfc5545.DateTime;

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
        // effective due is either the actual due, start + duration or absent
        Optional<DateTime> effectiveDue = new FirstPresent<>(
                new Seq<>(
                        new NullSafe<>(mTaskAdapter.valueOf(TaskAdapter.DUE)),
                        new Zipped<>(start, new NullSafe<>(mTaskAdapter.valueOf(TaskAdapter.DURATION)), DateTime::addDuration)));

        Single<ContentValues> baseData = new Distant(mTaskAdapter.valueOf(TaskAdapter.IS_CLOSED) ? -1 : 0,
                new Enduring(new DueDated(effectiveDue, new StartDated(start, new VanillaInstanceData()))));

        // TODO: implement support for recurrence, for now we only return the first instance
        return new SingletonIterator<>(mTaskAdapter.isRecurring() ?
                new Overridden(new NullSafe<>(mTaskAdapter.valueOf(TaskAdapter.ORIGINAL_INSTANCE_TIME)), baseData)
                :
                baseData);
    }
}
