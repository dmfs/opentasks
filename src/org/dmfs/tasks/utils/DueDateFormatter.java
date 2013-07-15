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

package org.dmfs.tasks.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.dmfs.tasks.R;

import android.content.Context;
import android.text.format.Time;


/**
 * Helper class to format a due date to present it to the user.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 */
public class DueDateFormatter
{
	/**
	 * The formatter we use for due dates other than today.
	 */
	private final DateFormat mDateFormatter = DateFormat.getDateInstance(SimpleDateFormat.MEDIUM);

	/**
	 * The formatter we use for tasks that are due today.
	 */
	private final DateFormat mTimeFormatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);

	/**
	 * A context to load resource string.
	 */
	private Context mContext;

	/**
	 * A helper to get the current date & time.
	 */
	private Time mNow;


	public DueDateFormatter(Context context)
	{
		mContext = context;
		mNow = new Time();
	}


	/**
	 * Format the given due date. The result depends on the current date and on the all-day flag of the due date.
	 * <p>
	 * If the due date it today the format will contain "today" instead of the date. Allday dates won't contain a time.
	 * </p>
	 * 
	 * 
	 * @param dueDate
	 *            The due date to format.
	 * @return A string with the formatted due date.
	 */
	public String format(Time dueDate)
	{
		mNow.clear(TimeZone.getDefault().getID());
		mNow.setToNow();

		if (!dueDate.allDay)
		{
			dueDate.switchTimezone(TimeZone.getDefault().getID());
		}

		// normalize time to ensure yearDay is set properly
		dueDate.normalize(false);

		if (dueDate.year == mNow.year && dueDate.yearDay == mNow.yearDay)
		{
			if (dueDate.allDay)
			{
				return mContext.getString(R.string.today);
			}
			else
			{
				return mContext.getString(R.string.today) + ", " + mTimeFormatter.format(new Date(dueDate.toMillis(false)));
			}
		}
		else
		{
			return mDateFormatter.format(new Date(dueDate.toMillis(false)));
		}

	}
}
