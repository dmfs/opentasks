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
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

import org.dmfs.tasks.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;
import android.text.format.Time;


/**
 * Helper class to format a due date to present it to the user.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class DueDateFormatter
{
	private final static int DEFAULT_DATEUTILS_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE
		| DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;
	private final static int TIME_DATEUTILS_FLAGS = DateUtils.FORMAT_SHOW_TIME;

	/**
	 * The formatter we use for due dates other than today.
	 */
	private final DateFormat mDateFormatter = DateFormat.getDateInstance(SimpleDateFormat.MEDIUM);

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
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public String format(Time dueDate)
	{
		mNow.clear(TimeZone.getDefault().getID());
		mNow.setToNow();

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
				return mContext.getString(R.string.today) + ", " + DateUtils.formatDateTime(mContext, dueDate.toMillis(false), TIME_DATEUTILS_FLAGS).toString();
			}
		}
		else
		{

			if (dueDate.allDay)
			{
				// use DataRange in order to set the correct timezone
				if (Build.VERSION.SDK_INT > 8)
				{
					return DateUtils.formatDateRange(mContext, new Formatter(Locale.getDefault()), dueDate.toMillis(false), dueDate.toMillis(false),
						DEFAULT_DATEUTILS_FLAGS, "UTC").toString();
				}
				else
				{
					mDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
					return mDateFormatter.format(new Date(dueDate.toMillis(false)));
				}

			}
			return DateUtils.formatDateTime(mContext, dueDate.toMillis(false), DEFAULT_DATEUTILS_FLAGS).toString();

		}
	}
}
