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

import java.util.TimeZone;

import org.dmfs.rfc5545.DateTime;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * Knows how to load and store {@link DateTime} values from a {@link Cursor} or {@link ContentValues}.
 * 
 * {@link DateTime} values are stored as three separate values:
 * <ul>
 * <li>a timestamp in milliseconds since the epoch</li>
 * <li>a time zone</li>
 * <li>an allday flag</li>
 * </ul>
 * 
 * This adapter combines those three fields to a {@link DateTime} value. If the time zone field is <code>null</code> the time zone is always set to UTC.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @param <EntityType>
 *            The type of the entity the field belongs to.
 */
public final class DateTimeFieldAdapter<EntityType> extends SimpleFieldAdapter<DateTime, EntityType>
{
	private final String mTimestampField;
	private final String mTzField;
	private final String mAllDayField;
	private final boolean mAllDayDefault;


	/**
	 * Constructor for a new {@link DateTimeFieldAdapter}.
	 * 
	 * @param timestampField
	 *            The name of the field that holds the time stamp in milliseconds.
	 * @param tzField
	 *            The name of the field that holds the time zone (as Olson ID). If the field name is <code>null</code> the time is always set to UTC.
	 * @param alldayField
	 *            The name of the field that indicated that this time is a date not a date-time. If this fieldName is <code>null</code> all loaded values are
	 *            non-allday.
	 */
	public DateTimeFieldAdapter(String timestampField, String tzField, String alldayField)
	{
		if (timestampField == null)
		{
			throw new IllegalArgumentException("timestampField must not be null");
		}
		mTimestampField = timestampField;
		mTzField = tzField;
		mAllDayField = alldayField;
		mAllDayDefault = false;
	}


	@Override
	String fieldName()
	{
		return mTimestampField;
	}


	@Override
	public DateTime getFrom(ContentValues values)
	{
		Long timestamp = values.getAsLong(mTimestampField);
		if (timestamp == null)
		{
			// if the time stamp is null we return null
			return null;
		}
		// create a new Time for the given time zone, falling back to UTC if none is given
		String timezone = mTzField == null ? null : values.getAsString(mTzField);
		DateTime value = new DateTime(timezone == null ? DateTime.UTC : TimeZone.getTimeZone(timezone), timestamp);

		// cache mAlldayField locally
		String allDayField = mAllDayField;

		// set the allday flag appropriately
		Integer allDayInt = allDayField == null ? null : values.getAsInteger(allDayField);

		if ((allDayInt != null && allDayInt != 0) || (allDayField == null && mAllDayDefault))
		{
			value = value.toAllDay();
		}

		return value;
	}


	@Override
	public DateTime getFrom(Cursor cursor)
	{
		int tsIdx = cursor.getColumnIndex(mTimestampField);
		int tzIdx = mTzField == null ? -1 : cursor.getColumnIndex(mTzField);
		int adIdx = mAllDayField == null ? -1 : cursor.getColumnIndex(mAllDayField);

		if (tsIdx < 0 || (mTzField != null && tzIdx < 0) || (mAllDayField != null && adIdx < 0))
		{
			throw new IllegalArgumentException("At least one column is missing in cursor.");
		}

		if (cursor.isNull(tsIdx))
		{
			// if the time stamp is null we return null
			return null;
		}

		Long timestamp = cursor.getLong(tsIdx);

		// create a new Time for the given time zone, falling back to UTC if none is given
		String timezone = mTzField == null ? null : cursor.getString(tzIdx);
		DateTime value = new DateTime(timezone == null ? DateTime.UTC : TimeZone.getTimeZone(timezone), timestamp);

		// set the allday flag appropriately
		Integer allDayInt = adIdx < 0 ? null : cursor.getInt(adIdx);

		if ((allDayInt != null && allDayInt != 0) || (mAllDayField == null && mAllDayDefault))
		{
			value = value.toAllDay();
		}
		return value;
	}


	@Override
	public DateTime getFrom(Cursor cursor, ContentValues values)
	{
		int tsIdx;
		int tzIdx;
		int adIdx;
		long timestamp;
		String timeZoneId = null;
		Integer allDay = 0;

		if (values != null && values.containsKey(mTimestampField))
		{
			if (values.getAsLong(mTimestampField) == null)
			{
				// if the time stamp is null we return null
				return null;
			}
			timestamp = values.getAsLong(mTimestampField);
		}
		else if (cursor != null && (tsIdx = cursor.getColumnIndex(mTimestampField)) >= 0)
		{
			if (cursor.isNull(tsIdx))
			{
				// if the time stamp is null we return null
				return null;
			}
			timestamp = cursor.getLong(tsIdx);
		}
		else
		{
			throw new IllegalArgumentException("Missing timestamp column.");
		}

		if (mTzField != null)
		{
			if (values != null && values.containsKey(mTzField))
			{
				timeZoneId = values.getAsString(mTzField);
			}
			else if (cursor != null && (tzIdx = cursor.getColumnIndex(mTzField)) >= 0)
			{
				timeZoneId = cursor.getString(tzIdx);
			}
			else
			{
				throw new IllegalArgumentException("Missing timezone column.");
			}
		}

		if (mAllDayField != null)
		{
			if (values != null && values.containsKey(mAllDayField))
			{
				allDay = values.getAsInteger(mAllDayField);
			}
			else if (cursor != null && (adIdx = cursor.getColumnIndex(mAllDayField)) >= 0)
			{
				allDay = cursor.getInt(adIdx);
			}
			else
			{
				throw new IllegalArgumentException("Missing timezone column.");
			}
		}

		// create a new Time for the given time zone, falling back to UTC if none is given
		DateTime value = new DateTime(timeZoneId == null ? DateTime.UTC : TimeZone.getTimeZone(timeZoneId), timestamp);

		if (allDay != 0)
		{
			value = value.toAllDay();
		}
		return value;
	}


	@Override
	public void setIn(ContentValues values, DateTime value)
	{
		if (value != null)
		{
			// just store all three parts separately
			values.put(mTimestampField, value.getTimestamp());

			if (mTzField != null)
			{
				TimeZone timezone = value.getTimeZone();
				values.put(mTzField, timezone == null ? null : timezone.getID());
			}
			if (mAllDayField != null)
			{
				values.put(mAllDayField, value.isAllDay() ? 1 : 0);
			}
		}
		else
		{
			// write timestamp only, other fields may still use allday and timezone
			values.put(mTimestampField, (Long) null);
		}
	}
}
