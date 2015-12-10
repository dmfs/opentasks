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

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;

import android.text.format.Time;


/**
 * Ensure a time is not before a specific reference time. The new value will be set to the reference time otherwise.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class NotBefore extends AbstractConstraint<Time>
{
	private final TimeFieldAdapter mTimeAdapter;


	public NotBefore(TimeFieldAdapter adapter)
	{
		mTimeAdapter = adapter;
	}


	@Override
	public Time apply(ContentSet currentValues, Time oldValue, Time newValue)
	{
		Time notBeforeThisTime = mTimeAdapter.get(currentValues);
		if (notBeforeThisTime != null && newValue != null)
		{
			if (newValue.before(notBeforeThisTime))
			{
				newValue.set(notBeforeThisTime);
			}
		}
		return newValue;
	}

}
