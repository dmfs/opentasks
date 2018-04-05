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

import org.dmfs.opentaskspal.datetime.general.StartOfHour;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.adapters.FieldAdapter;


/**
 * Provides a value which defaults to NOW, but is always before a specific reference value.
 * All-day values are set to the previous day, date-time values are set to the start of the hour of the reference value.
 */
public class DefaultBefore implements Default<DateTime>
{

    private final FieldAdapter<DateTime> mReferenceAdapter;


    public DefaultBefore(FieldAdapter<DateTime> referenceAdapter)
    {
        mReferenceAdapter = referenceAdapter;
    }


    @Override
    public DateTime getCustomDefault(ContentSet currentValues, DateTime genericDefault)
    {
        DateTime reference = mReferenceAdapter != null ? mReferenceAdapter.get(currentValues) : null;
        boolean useReference = reference != null && !genericDefault.before(reference);
        DateTime value = useReference ? reference : genericDefault;
        if (value.isAllDay())
        {
            if (useReference)
            {
                value = value.addDuration(new Duration(-1, 1, 0));
            }
        }
        else
        {
            value = new StartOfHour(value).value();
        }
        return value;
    }
}
