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

import org.dmfs.iterators.AbstractBaseIterator;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSetIterator;

import java.util.Iterator;


/**
 * An {@link Iterator} of instances as returned by a {@link RecurrenceSetIterator}.
 * <p>
 * TODO: this should go to lib-recur
 *
 * @author Marten Gajda
 */
public final class TaskInstanceIterator extends AbstractBaseIterator<DateTime>
{
    private final DateTime mStart;
    private final RecurrenceSetIterator mSetIterator;
    private final String mTimezone;


    TaskInstanceIterator(DateTime start, RecurrenceSetIterator setIterator, String timezone)
    {
        mStart = start;
        mSetIterator = setIterator;
        mTimezone = timezone;
    }


    @Override
    public boolean hasNext()
    {
        return mSetIterator.hasNext();
    }


    @Override
    public DateTime next()
    {
        DateTime result = new DateTime(mStart.getTimeZone(), mSetIterator.next());
        return mStart.isAllDay() ? result.toAllDay() : mTimezone == null ? result.swapTimeZone(null) : result;
    }
}
