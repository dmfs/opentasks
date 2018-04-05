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

import org.dmfs.opentaskspal.datetime.general.MillisDuration;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.defaults.Default;
import org.dmfs.tasks.model.defaults.DefaultAfter;


/**
 * Ensure a time is before a specific reference time.
 * Otherwise, shift the reference time by the same amount that value has been shifted.
 * If this still violated the constraint, then set the reference value to its default value.
 */
public class BeforeOrShiftTime extends AbstractConstraint<DateTime>
{
    private final FieldAdapter<DateTime> mReferenceAdapter;
    private final Default<DateTime> mDefault;


    public BeforeOrShiftTime(FieldAdapter<DateTime> referenceAdapter)
    {
        mReferenceAdapter = referenceAdapter;
        mDefault = new DefaultAfter(null);
    }


    @Override
    public DateTime apply(ContentSet currentValues, DateTime oldValue, DateTime newValue)
    {
        DateTime reference = mReferenceAdapter.get(currentValues);
        if (reference != null && newValue != null)
        {
            if (oldValue != null && !newValue.before(reference))
            {
                // try to shift the reference value
                long diff = newValue.getTimestamp() - oldValue.getTimestamp();
                if (diff > 0)
                {
                    boolean isAllDay = reference.isAllDay();
                    reference = reference.addDuration(new MillisDuration(diff).value());
                    // ensure the event is still allday if is was allday before.
                    if (isAllDay)
                    {
                        reference = reference.toAllDay();
                    }
                    mReferenceAdapter.set(currentValues, reference);
                }
            }
            if (!newValue.before(reference))
            {
                // constraint is still violated, so set reference to its default value
                reference = mDefault.getCustomDefault(currentValues, newValue);
                mReferenceAdapter.set(currentValues, reference);
            }
        }
        return newValue;
    }

}
