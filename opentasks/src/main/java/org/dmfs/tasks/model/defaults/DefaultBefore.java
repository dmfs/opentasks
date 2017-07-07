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

import android.text.format.Time;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.adapters.FieldAdapter;


/**
 * Provides a value which defaults to NOW, but is always before a specific reference value.
 * All-day values are set to the previous day, date-time values are set to the start of the hour of the reference value.
 */
public class DefaultBefore implements Default<Time>
{

    private final FieldAdapter<Time> mReferenceAdapter;


    public DefaultBefore(FieldAdapter<Time> referenceAdapter)
    {
        mReferenceAdapter = referenceAdapter;
    }


    @Override
    public Time getCustomDefault(ContentSet currentValues, Time genericDefault)
    {
        Time reference = mReferenceAdapter != null ? mReferenceAdapter.get(currentValues) : null;
        boolean useReference = reference != null && !genericDefault.before(reference);
        Time value = new Time(useReference ? reference : genericDefault);
        if (value.allDay)
        {
            value.set(value.monthDay - (useReference ? 1 : 0), value.month, value.year);
        }
        else
        {
            value.minute--;
            value.normalize(false);
            value.second = 0;
            value.minute = 0;
        }
        return value;
    }
}
