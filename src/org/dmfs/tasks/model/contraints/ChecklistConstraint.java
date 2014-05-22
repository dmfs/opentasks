/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
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
 * 
 */

package org.dmfs.tasks.model.contraints;

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.adapters.IntegerFieldAdapter;


/**
 * Adjust percent complete & status when a checklist is changed.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ChecklistConstraint extends AbstractConstraint<String>
{
	private final IntegerFieldAdapter mPercentCompleteAdapter;
	private final IntegerFieldAdapter mStatusAdapter;


	public ChecklistConstraint(IntegerFieldAdapter statusAdapter, IntegerFieldAdapter percentCompleteAdapter)
	{
		mPercentCompleteAdapter = percentCompleteAdapter;
		mStatusAdapter = statusAdapter;
	}


	@Override
	public String apply(ContentSet currentValues, String oldValue, String newValue)
	{
		if (newValue != null && (newValue.contains("[x]") || newValue.contains("[X]") || newValue.contains("[ ]")))
		{
			/*
			 * Looks like a checklist!
			 * 
			 * Now we do the following:
			 * 
			 * 1) Count the number of lines and the number of checked lines
			 * 
			 * 2) Update status accordingly
			 * 
			 * 3) Update percent complete accordingly
			 */

			String[] items = newValue.split("\n");

			int count = 0;
			int checked = 0;

			for (int i = 0, lineCount = items.length; i < lineCount; ++i)
			{
				String item = items[i];
				if (item.startsWith("[x]") || item.startsWith("[X]"))
				{
					checked++;
					count++;
				}
				else if (item.startsWith("[ ]"))
				{
					count++;
				}
			}

			if (count > 0)
			{
				int newPercentComplete = (checked * 100) / count;

				if (mStatusAdapter != null)
				{
					Integer oldStatus = mStatusAdapter.get(currentValues);
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
