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

import org.dmfs.jems.single.combined.Backed;
import org.dmfs.optional.NullSafe;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recurrenceset.RecurrenceList;
import org.dmfs.rfc5545.recurrenceset.RecurrenceRuleAdapter;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSet;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSetIterator;

import java.util.Iterator;


/**
 * An {@link Iterable} of all the instances of a task.
 *
 * @author Marten Gajda
 */
public final class TaskInstanceIterable implements Iterable<DateTime>
{
    private final TaskAdapter mTaskAdapter;


    public TaskInstanceIterable(TaskAdapter taskAdapter)
    {
        mTaskAdapter = taskAdapter;
    }


    @Override
    public Iterator<DateTime> iterator()
    {
        DateTime dtstart = new Backed<DateTime>(new NullSafe<>(mTaskAdapter.valueOf(TaskAdapter.DTSTART)), () -> mTaskAdapter.valueOf(TaskAdapter.DUE)).value();

        RecurrenceSet set = new RecurrenceSet();
        RecurrenceRule rule = mTaskAdapter.valueOf(TaskAdapter.RRULE);
        if (rule != null)
        {
            set.addInstances(new RecurrenceRuleAdapter(rule));
        }

        set.addInstances(new RecurrenceList(toLongArray(mTaskAdapter.valueOf(TaskAdapter.RDATE))));
        set.addExceptions(new RecurrenceList(toLongArray(mTaskAdapter.valueOf(TaskAdapter.EXDATE))));

        RecurrenceSetIterator setIterator = set.iterator(dtstart.getTimeZone(), dtstart.getTimestamp(),
                System.currentTimeMillis() + 10L * 356L * 3600L * 1000L);

        return new TaskInstanceIterator(dtstart, setIterator, mTaskAdapter.valueOf(TaskAdapter.TIMEZONE_RAW));
    }


    private long[] toLongArray(Iterable<DateTime> dates)
    {
        int count = 0;
        for (DateTime ignored : dates)
        {
            count += 1;
        }
        long[] timeStamps = new long[count];
        int i = 0;
        for (DateTime dt : dates)
        {
            timeStamps[i++] = dt.getTimestamp();
        }
        return timeStamps;
    }
}
