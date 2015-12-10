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
 * Adjust percent complete to 0% if status is set to NEEDS_ACTION. Also sets percent complete to 50% if status is changed from COMPLETED to IN_PROCESS.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class AdjustPercentComplete extends AbstractConstraint<Integer>
{
	private final IntegerFieldAdapter mPercentComplete;


	public AdjustPercentComplete(IntegerFieldAdapter adapter)
	{
		mPercentComplete = adapter;
	}


	@Override
	public Integer apply(ContentSet currentValues, Integer oldValue, Integer newValue)
	{
		if (newValue == null || newValue == Tasks.STATUS_NEEDS_ACTION)
		{
			mPercentComplete.set(currentValues, 0);
		}
		else if (newValue == Tasks.STATUS_IN_PROCESS && oldValue != null && oldValue == Tasks.STATUS_COMPLETED && mPercentComplete.get(currentValues) == 100)
		{
			mPercentComplete.set(currentValues, 50);
		}
		return newValue;
	}

}
