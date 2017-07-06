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
 * Ensure a time is after a specific reference time. The new value will be set using {@link DefaultAfter} otherwise.
 */
public class After extends AbstractConstraint<Time>
{
    private final FieldAdapter<Time> mReferenceAdapter;
    private final Default<Time> mDefault;


    public After(FieldAdapter<Time> referenceAdapter)
    {
        mReferenceAdapter = referenceAdapter;
        mDefault = new DefaultAfter(referenceAdapter);
    }


    @Override
    public Time apply(ContentSet currentValues, Time oldValue, Time newValue)
    {
        Time reference = mReferenceAdapter.get(currentValues);
        if (reference != null && newValue != null && !newValue.after(reference))
        {
            newValue.set(mDefault.getCustomDefault(currentValues, reference));
        }
        return newValue;
    }

}
