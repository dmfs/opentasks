/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.provider.tasks.model.adapters;

import java.io.IOException;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.dmfs.rfc5545.DateTime;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * Knows how to load and store arrays of {@link DateTime} values from a {@link Cursor} or {@link ContentValues}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @param <EntityType>
 *            The type of the entity the field belongs to.
 */
public final class DateTimeArrayFieldAdapter<EntityType> extends SimpleFieldAdapter<DateTime[], EntityType>
{
	private final static Pattern SEPARATOR_PATTERN = Pattern.compile(",");

	private final String mDateTimeListFieldName;
	private final String mTimeZoneFieldName;


	/**
	 * Constructor for a new {@link DateTimeArrayFieldAdapter}.
	 * 
	 * @param datetimeListFieldName
	 *            The name of the field that holds the {@link DateTime} list.
	 * @param timezoneFieldName
	 *            The name of the field that holds the time zone name.
	 */
	public DateTimeArrayFieldAdapter(String datetimeListFieldName, String timezoneFieldName)
	{
		if (datetimeListFieldName == null)
		{
			throw new IllegalArgumentException("datetimeListFieldName must not be null");
		}
		mDateTimeListFieldName = datetimeListFieldName;
		mTimeZoneFieldName = timezoneFieldName;
	}


	@Override
	String fieldName()
	{
		return mDateTimeListFieldName;
	}


	@Override
	public DateTime[] getFrom(ContentValues values)
	{
		String datetimeList = values.getAsString(mDateTimeListFieldName);
		if (datetimeList == null)
		{
			// no list, return null
			return null;
		}

		// create a new TimeZone for the given time zone string
		String timezoneString = mTimeZoneFieldName == null ? null : values.getAsString(mTimeZoneFieldName);
		TimeZone timeZone = timezoneString == null ? null : TimeZone.getTimeZone(timezoneString);

		String[] datetimes = SEPARATOR_PATTERN.split(datetimeList);

		DateTime[] result = new DateTime[datetimes.length];
		for (int i = 0, count = datetimes.length; i < count; ++i)
		{
			DateTime value = DateTime.parse(timeZone, datetimes[i]);

			if (!value.isAllDay() && value.isFloating())
			{
				throw new IllegalArgumentException("DateTime values must not be floating, unless they are all-day.");
			}

			result[i] = value;
			if (i > 0 && result[0].isAllDay() != value.isAllDay())
			{
				throw new IllegalArgumentException("DateTime values must all be of the same type.");
			}
		}

		return result;
	}


	@Override
	public DateTime[] getFrom(Cursor cursor)
	{
		int tdLIdx = cursor.getColumnIndex(mDateTimeListFieldName);
		int tzIdx = mTimeZoneFieldName == null ? -1 : cursor.getColumnIndex(mTimeZoneFieldName);

		if (tdLIdx < 0 || (mTimeZoneFieldName != null && tzIdx < 0))
		{
			throw new IllegalArgumentException("At least one column is missing in cursor.");
		}

		if (cursor.isNull(tdLIdx))
		{
			// if the time stamp list is null we return null
			return null;
		}

		String datetimeList = cursor.getString(tdLIdx);

		// create a new TimeZone for the given time zone string
		String timezoneString = mTimeZoneFieldName == null ? null : cursor.getString(tzIdx);
		TimeZone timeZone = timezoneString == null ? null : TimeZone.getTimeZone(timezoneString);

		String[] datetimes = SEPARATOR_PATTERN.split(datetimeList);

		DateTime[] result = new DateTime[datetimes.length];
		for (int i = 0, count = datetimes.length; i < count; ++i)
		{
			DateTime value = DateTime.parse(timeZone, datetimes[i]);

			if (!value.isAllDay() && value.isFloating())
			{
				throw new IllegalArgumentException("DateTime values must not be floating, unless they are all-day.");
			}

			result[i] = value;
			if (i > 0 && result[0].isAllDay() != value.isAllDay())
			{
				throw new IllegalArgumentException("DateTime values must all be of the same type.");
			}
		}

		return result;
	}


	@Override
	public DateTime[] getFrom(Cursor cursor, ContentValues values)
	{
		int tsIdx;
		int tzIdx;
		String datetimeList;
		String timeZoneId = null;

		if (values != null && values.containsKey(mDateTimeListFieldName))
		{
			if (values.getAsLong(mDateTimeListFieldName) == null)
			{
				// the date times are null, so we return null
				return null;
			}
			datetimeList = values.getAsString(mDateTimeListFieldName);
		}
		else if (cursor != null && (tsIdx = cursor.getColumnIndex(mDateTimeListFieldName)) >= 0)
		{
			if (cursor.isNull(tsIdx))
			{
				// the date times are null, so we return null
				return null;
			}
			datetimeList = cursor.getString(tsIdx);
		}
		else
		{
			throw new IllegalArgumentException("Missing date time list column.");
		}

		if (mTimeZoneFieldName != null)
		{
			if (values != null && values.containsKey(mTimeZoneFieldName))
			{
				timeZoneId = values.getAsString(mTimeZoneFieldName);
			}
			else if (cursor != null && (tzIdx = cursor.getColumnIndex(mTimeZoneFieldName)) >= 0)
			{
				timeZoneId = cursor.getString(tzIdx);
			}
			else
			{
				throw new IllegalArgumentException("Missing timezone column.");
			}
		}

		// create a new TimeZone for the given time zone string
		TimeZone timeZone = timeZoneId == null ? null : TimeZone.getTimeZone(timeZoneId);

		String[] datetimes = SEPARATOR_PATTERN.split(datetimeList);

		DateTime[] result = new DateTime[datetimes.length];
		for (int i = 0, count = datetimes.length; i < count; ++i)
		{
			DateTime value = DateTime.parse(timeZone, datetimes[i]);

			if (!value.isAllDay() && value.isFloating())
			{
				throw new IllegalArgumentException("DateTime values must not be floating, unless they are all-day.");
			}

			result[i] = value;
			if (i > 0 && result[0].isAllDay() != value.isAllDay())
			{
				throw new IllegalArgumentException("DateTime values must all be of the same type.");
			}
		}

		return result;
	}


	@Override
	public void setIn(ContentValues values, DateTime[] value)
	{
		if (value != null && value.length > 0)
		{
			try
			{
				// Note: we only store the datetime strings, not the timezone
				StringBuilder result = new StringBuilder(value.length * 17 /* this is the maximum length */);

				boolean first = true;
				for (DateTime datetime : value)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						result.append(',');
					}
					DateTime outvalue = datetime.isFloating() ? datetime : datetime.shiftTimeZone(DateTime.UTC);
					outvalue.writeTo(result);
				}
				values.put(mDateTimeListFieldName, result.toString());
			}
			catch (IOException e)
			{
				throw new RuntimeException("Can not serialize datetime list.");
			}

		}
		else
		{
			values.put(mDateTimeListFieldName, (Long) null);
		}
	}
}
