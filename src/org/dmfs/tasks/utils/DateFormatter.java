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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;
import android.text.format.Time;


/**
 * Helper class to format a date to present it to the user.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * @author Marten Gajda <marten@dmfs.org>
 */
public class DateFormatter
{

	public enum DateFormatContext
	{

		/**
		 * Always uses a relative date. Use this when the date is with the past or next 6 days, otherwise you might get an absolute date.
		 */
		RELATIVE {
			@Override
			public boolean useRelative(Time now, Time date)
			{
				return Math.abs(date.toMillis(false) - now.toMillis(false)) < 7 * 24 * 3600 * 1000;
			}
		},

		/**
		 * The date format in the details view.
		 */
		DETAILS_VIEW {

			@Override
			public int getDateUtilsFlags(Time now, Time date)
			{
				if (date.allDay)
				{
					return DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;
				}
				else
				{
					return DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY
						| DateUtils.FORMAT_SHOW_TIME;
				}
			}
		},

		/**
		 * The date format in the list view.
		 * 
		 * Currently this inherits the default short format.
		 */
		LIST_VIEW {
			@Override
			public boolean useRelative(Time now, Time date)
			{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				{
					return Math.abs(date.toMillis(false) - now.toMillis(false)) < 7 * 24 * 3600 * 1000;
				}
				// The DateUtils implementation of pre ICS android calculates the relative date times in fixed 24h slots and therefore is unusable for us.
				return false;

			}
		},

		/**
		 * The date format in the widget.
		 * 
		 * Currently this inherits the default short format.
		 */
		WIDGET_VIEW {
			@Override
			public int getDateUtilsFlags(Time now, Time date)
			{
				int result = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;
				if (!date.allDay)
				{
					result |= DateUtils.FORMAT_SHOW_TIME;
				}
				if (now.year != date.year)
				{
					result |= DateUtils.FORMAT_SHOW_YEAR;
				}
				return result;
			}
		},

		/**
		 * The date format in the dash clock. This shows a time only.
		 */
		DASHCLOCK_VIEW {

			@Override
			public int getDateUtilsFlags(Time now, Time date)
			{
				return DateUtils.FORMAT_SHOW_TIME;
			}
		},

		/**
		 * The date format in notifications.
		 */
		NOTIFICATION_VIEW_DATE {

			@Override
			public int getDateUtilsFlags(Time now, Time date)
			{
				return DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;
			}


			@Override
			public boolean useRelative(Time now, Time date)
			{
				return true;
			}
		},

		/**
		 * The date format in notifications.
		 */
		NOTIFICATION_VIEW_TIME {

			@Override
			public int getDateUtilsFlags(Time now, Time date)
			{
				return DateUtils.FORMAT_SHOW_TIME;
			}


			@Override
			public boolean useRelative(Time now, Time date)
			{
				return false;
			}
		};

		public int getDateUtilsFlags(Time now, Time date)
		{
			if (now.year == date.year && now.yearDay == date.yearDay)
			{
				// today, show time only
				return DateUtils.FORMAT_SHOW_TIME;
			}
			else if (now.year == date.year)
			{
				// this year, don't include the year
				return DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;
			}
			else
			{
				return DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_WEEKDAY
					| DateUtils.FORMAT_ABBREV_WEEKDAY;
			}
		}


		public boolean useRelative(Time now, Time date)
		{
			return false;
		}
	}

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


	public DateFormatter(Context context)
	{
		mContext = context;
		mNow = new Time();
	}


	/**
	 * Format the given due date. The result depends on the current date and on the all-day flag of the due date.
	 * 
	 * 
	 * @param date
	 *            The due date to format.
	 * @param useToday
	 *            <code>true</code> to write "today" instead of the date when the date is on the present day
	 * @return A string with the formatted due date.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public String format(Time date, DateFormatContext dateContext)
	{
		mNow.clear(TimeZone.getDefault().getID());
		mNow.setToNow();
		return format(date, mNow, dateContext);
	}


	/**
	 * Format the given due date. The result depends on the current date and on the all-day flag of the due date.
	 * 
	 * 
	 * @param date
	 *            The due date to format.
	 * @param useToday
	 *            <code>true</code> to write "today" instead of the date when the date is on the present day
	 * @return A string with the formatted due date.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public String format(Time date, Time now, DateFormatContext dateContext)
	{

		// normalize time to ensure yearDay is set properly
		date.normalize(false);

		if (dateContext.useRelative(now, date))
		{
			long delta = Math.abs(now.toMillis(false) - date.toMillis(false));

			if (date.allDay)
			{
				Time allDayNow = new Time("UTC");
				allDayNow.set(now.monthDay, now.month, now.year);
				return DateUtils.getRelativeTimeSpanString(date.toMillis(false), allDayNow.toMillis(false), DateUtils.DAY_IN_MILLIS).toString();
			}
			else if (delta < 60 * 1000)
			{
				// the time is within this minute, show "now"
				return mContext.getString(R.string.now);
			}
			else if (delta < 60 * 60 * 1000)
			{
				// time is within this hour, show number of minutes left
				return DateUtils.getRelativeTimeSpanString(date.toMillis(false), now.toMillis(false), DateUtils.MINUTE_IN_MILLIS).toString();
			}
			else if (delta < 24 * 60 * 60 * 1000)
			{
				// time is within 24 hours, show relative string with time
				// FIXME: instead of using a fixed 24 hour interval this should be aligned to midnight tomorrow and yesterday
				return DateUtils.getRelativeDateTimeString(mContext, date.toMillis(false), DateUtils.DAY_IN_MILLIS, DateUtils.WEEK_IN_MILLIS,
					dateContext.getDateUtilsFlags(now, date)).toString();
			}
			else
			{
				return DateUtils.getRelativeTimeSpanString(date.toMillis(false), now.toMillis(false), DateUtils.DAY_IN_MILLIS).toString();
			}
		}

		return date.allDay ? formatAllDay(date, now, dateContext) : formatNonAllDay(date, now, dateContext);
	}


	@SuppressLint("NewApi")
	private String formatAllDay(Time date, Time now, DateFormatContext dateContext)
	{
		// use DataRange in order to set the correct timezone
		if (Build.VERSION.SDK_INT > 8)
		{
			return DateUtils.formatDateRange(mContext, new Formatter(Locale.getDefault()), date.toMillis(false), date.toMillis(false),
				dateContext.getDateUtilsFlags(now, date), "UTC").toString();
		}
		else
		{
			mDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			return mDateFormatter.format(new Date(date.toMillis(false)));
		}
	}


	private String formatNonAllDay(Time date, Time now, DateFormatContext dateContext)
	{
		return DateUtils.formatDateTime(mContext, date.toMillis(false), dateContext.getDateUtilsFlags(now, date));
	}
}
