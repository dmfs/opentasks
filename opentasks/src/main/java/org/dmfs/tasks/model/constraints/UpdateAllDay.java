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
 * Updates the value returned by a {@link FieldAdapter} of type Time when the all-day flag changes. We need this if a task model doesn't support start or due
 * dates. The sync adapter might still store those values, so we need to update them when the all-day flag changes.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class UpdateAllDay extends AbstractConstraint<Boolean>
{
    private final FieldAdapter<Time> mTimeAdapter;


    public UpdateAllDay(FieldAdapter<Time> adapter)
    {
        mTimeAdapter = adapter;
    }


    @Override
    public Boolean apply(ContentSet currentValues, Boolean oldValue, Boolean newValue)
    {
        Time time = mTimeAdapter.get(currentValues);
        if (time != null)
        {
            if ((oldValue == null || !oldValue) && newValue != null && newValue)
            {
                // all-day has been enabled, ensure the given time is all-day

                if (time.toMillis(false) % (24L * 60 * 60 * 1000L) != 0)
                {
                    // not at 00:00:00 UTC yet
                    time.timezone = "UTC";
                    time.set(time.monthDay, time.month, time.year);
                    mTimeAdapter.set(currentValues, time);
                }

            }
            // else if ((newValue == null || !newValue) && oldValue != null && oldValue)
             // {
                // ideally we move the time to 00:00:00 in the new time zone. Unfortunately we don't know the time zone at this point
                // TODO: move the time to 00:00:00 in the new time zone, somehow
            // }
        }
        return newValue;
    }

}
