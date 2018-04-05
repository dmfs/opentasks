/*
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
 *
 */

package org.dmfs.tasks.model.defaults;

import org.dmfs.opentaskspal.datetime.general.StartOfNextHour;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.adapters.FieldAdapter;


/**
 * Provides a value which defaults to NOW, but is always after a specific reference value.
 * All-day values are set to the next day, date-time values are set to the next hour of the reference value.
 */
public class DefaultAfter implements Default<DateTime>
{

    private final FieldAdapter<DateTime> mReferenceAdapter;


    public DefaultAfter(FieldAdapter<DateTime> referenceAdapter)
    {
        mReferenceAdapter = referenceAdapter;
    }


    @Override
    public DateTime getCustomDefault(ContentSet currentValues, DateTime genericDefault)
    {
        DateTime reference = mReferenceAdapter != null ? mReferenceAdapter.get(currentValues) : null;
        boolean useReference = reference != null && !genericDefault.after(reference);
        DateTime value = useReference ? reference : genericDefault;
        if (value.isAllDay())
        {
            value = value.addDuration(new Duration(1, 1, 0));
        }
        else
        {
            value = new StartOfNextHour(value).value();
        }
        return value;
    }
}
