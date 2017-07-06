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

package org.dmfs.tasks.model.constraints;

import android.text.format.Time;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.adapters.FieldAdapter;


/**
 * Shift a reference time by the same amount that value has been shifted.
 * <p>
 * TODO: use Duration class to get the duration in days and shift without summer/winter time switches
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ShiftTime extends AbstractConstraint<Time>
{
    private final FieldAdapter<Time> mTimeAdapter;


    /**
     * Creates a new ShiftTime instance.
     *
     * @param adapter
     *         A {@link FieldAdapter<Time>} that knows how to load the value to shift.
     */
    public ShiftTime(FieldAdapter<Time> adapter)
    {
        mTimeAdapter = adapter;
    }


    @Override
    public Time apply(ContentSet currentValues, Time oldValue, Time newValue)
    {
        Time timeToShift = mTimeAdapter.get(currentValues);
        if (timeToShift != null && newValue != null && oldValue != null)
        {
            boolean isAllDay = timeToShift.allDay;
            timeToShift.set(timeToShift.toMillis(false) + (newValue.toMillis(false) - oldValue.toMillis(false)));

            // ensure the event is still allday if is was allday before.
            if (isAllDay)
            {
                timeToShift.set(timeToShift.monthDay, timeToShift.month, timeToShift.year);
            }
            mTimeAdapter.set(currentValues, timeToShift);
        }
        return newValue;
    }

}
