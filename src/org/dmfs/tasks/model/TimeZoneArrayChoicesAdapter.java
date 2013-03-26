/*
 * 
 *
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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
package org.dmfs.tasks.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TimeZone;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.adapters.TimezoneWrapper;

import android.content.Context;


/**
 * ArrayAdapter which loads an array of timezones from the resources file.
 * 
 * TODO: This looks more like a hack and it definitely needs some refactoring.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 * 
 */
public class TimeZoneArrayChoicesAdapter extends ResourceArrayChoicesAdapter
{

	private final ArrayList<Object> mTimeZoneIds;


	public TimeZoneArrayChoicesAdapter(Context context)
	{
		super(R.array.timezone_values, R.array.timezone_labels, context);
		int count = mChoices.size();
		mTimeZoneIds = new ArrayList<Object>();
		mTimeZoneIds.addAll(mChoices);
		for (int i = 0; i < count; ++i)
		{
			Object timezoneId = mChoices.get(i);
			mChoices.set(i, new TimezoneWrapper(timezoneId.toString()));
		}

		// sort time zones by raw offset
		Collections.sort(mChoices, new Comparator<Object>()
		{

			@Override
			public int compare(Object lhs, Object rhs)
			{
				if (lhs instanceof TimezoneWrapper && rhs instanceof TimezoneWrapper)
				{
					return ((TimezoneWrapper) lhs).getRawOffset() - ((TimezoneWrapper) rhs).getRawOffset();
				}
				return 0;
			}
		});
	}


	/**
	 * This function
	 * 
	 * @param object
	 *            The timezone string
	 */
	@Override
	public String getTitle(Object object)
	{
		if (object != null)
		{
			TimeZone selectedTimeZone = (TimeZone) object;
			String title = null;
			int index = mTimeZoneIds.indexOf(selectedTimeZone.getID());
			if (index >= 0)
			{
				title = mTitles.get(index);
			}

			if (title == null)
			{
				title = selectedTimeZone.getDisplayName();
			}
			return getGMTOffsetString(selectedTimeZone.getRawOffset()) + title;
		}
		return "";
	}


	private String getGMTOffsetString(long millis)
	{
		long absmillis = (millis < 0) ? -millis : millis;
		int minutes = (int) ((absmillis / (1000 * 60)) % 60);
		int hours = (int) ((absmillis / (1000 * 60 * 60)) % 24);
		StringBuilder builder = new StringBuilder("(GMT");
		builder.append((millis >= 0) ? '+' : '-');
		builder.append(String.format("%02d:%02d", hours, minutes)).append(") ");
		return builder.toString();
	}

}
