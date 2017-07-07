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

package org.dmfs.tasks.model.constraints;

import android.text.format.Time;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.defaults.Default;
import org.dmfs.tasks.model.defaults.DefaultAfter;


/**
 * Ensure a time is before a specific reference time.
 * Otherwise, shift the reference time by the same amount that value has been shifted.
 * If this still violated the constraint, then set the reference value to its default value.
 */
public class BeforeOrShiftTime extends AbstractConstraint<Time>
{
    private final FieldAdapter<Time> mReferenceAdapter;
    private final Default<Time> mDefault;


    public BeforeOrShiftTime(FieldAdapter<Time> referenceAdapter)
    {
        mReferenceAdapter = referenceAdapter;
        mDefault = new DefaultAfter(null);
    }


    @Override
    public Time apply(ContentSet currentValues, Time oldValue, Time newValue)
    {
        Time reference = mReferenceAdapter.get(currentValues);
        if (reference != null && newValue != null)
        {
            if (oldValue != null && !newValue.before(reference))
            {
                // try to shift the reference value
                long diff = newValue.toMillis(false) - oldValue.toMillis(false);
                if (diff > 0)
                {
                    boolean isAllDay = reference.allDay;
                    reference.set(reference.toMillis(false) + diff);

                    // ensure the event is still allday if is was allday before.
                    if (isAllDay)
                    {
                        reference.set(reference.monthDay, reference.month, reference.year);
                    }
                    mReferenceAdapter.set(currentValues, reference);
                }
            }
            if (!newValue.before(reference))
            {
                // constraint is still violated, so set reference to its default value
                reference.set(mDefault.getCustomDefault(currentValues, newValue));
                mReferenceAdapter.set(currentValues, reference);
            }
        }
        return newValue;
    }

}
