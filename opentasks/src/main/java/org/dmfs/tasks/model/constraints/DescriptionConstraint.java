/*
 * Copyright 2019 dmfs GmbH
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

import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.DescriptionItem;
import org.dmfs.tasks.model.adapters.IntegerFieldAdapter;

import java.util.List;


/**
 * Adjust percent complete & status when a checklist is changed.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class DescriptionConstraint extends AbstractConstraint<List<DescriptionItem>>
{
    private final IntegerFieldAdapter mPercentCompleteAdapter;
    private final IntegerFieldAdapter mStatusAdapter;


    public DescriptionConstraint(IntegerFieldAdapter statusAdapter, IntegerFieldAdapter percentCompleteAdapter)
    {
        mPercentCompleteAdapter = percentCompleteAdapter;
        mStatusAdapter = statusAdapter;
    }


    @Override
    public List<DescriptionItem> apply(ContentSet currentValues, List<DescriptionItem> oldValue, List<DescriptionItem> newValue)
    {
        if (oldValue != null && newValue != null && !oldValue.isEmpty() && !newValue.isEmpty() && !oldValue.equals(newValue))
        {
            int checked = 0;
            int checkbox = 0;
            for (DescriptionItem item : newValue)
            {
                if (item.checkbox)
                {
                    ++checkbox;
                    if (item.checked)
                    {
                        ++checked;
                    }
                }
            }

            if (checkbox > 0)
            {
                int newPercentComplete = (checked * 100) / checkbox;

                if (mStatusAdapter != null)
                {
                    Integer oldStatus = mStatusAdapter.get(currentValues);

                    if (oldStatus == null)
                    {
                        oldStatus = mStatusAdapter.getDefault(currentValues);
                    }

                    Integer newStatus = newPercentComplete == 100 ? Tasks.STATUS_COMPLETED : newPercentComplete > 0 || oldStatus != null
                            && oldStatus == Tasks.STATUS_COMPLETED ? Tasks.STATUS_IN_PROCESS : oldStatus;
                    if (oldStatus == null && newStatus != null || oldStatus != null && !oldStatus.equals(newStatus) && oldStatus != Tasks.STATUS_CANCELLED)
                    {
                        mStatusAdapter.set(currentValues, newStatus);
                    }
                }

                if (mPercentCompleteAdapter != null)
                {
                    Integer oldPercentComplete = mPercentCompleteAdapter.get(currentValues);
                    if (oldPercentComplete == null || oldPercentComplete != newPercentComplete)
                    {
                        mPercentCompleteAdapter.set(currentValues, newPercentComplete);
                    }
                }
            }
        }
        return newValue;
    }
}
