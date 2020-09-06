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

import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recurrenceset.RecurrenceList;
import org.dmfs.rfc5545.recurrenceset.RecurrenceRuleAdapter;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSet;

import java.util.Iterator;
import java.util.TimeZone;


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
            if (rule.getUntil() != null && dtstart.isFloating() != rule.getUntil().isFloating())
            {
                // rule UNTIL date mismatches start. This is merely a workaround for existing users. In future we should make sure
                // such tasks don't exist
                if (dtstart.isFloating())
                {
                    // make until floating too by making it floating in the current time zone
                    rule.setUntil(rule.getUntil().shiftTimeZone(TimeZone.getDefault()).swapTimeZone(null));
                }
                else
                {
                    // anchor UNTIL in the current time zone
                    rule.setUntil(new DateTime(null, rule.getUntil().getTimestamp()).swapTimeZone(TimeZone.getDefault()));
                }
            }
            set.addInstances(new RecurrenceRuleAdapter(rule));
        }

        set.addInstances(new RecurrenceList(new Timestamps(mTaskAdapter.valueOf(TaskAdapter.RDATE)).value()));
        set.addExceptions(new RecurrenceList(new Timestamps(mTaskAdapter.valueOf(TaskAdapter.EXDATE)).value()));

        return new TaskInstanceIterator(dtstart, set);
    }
}
