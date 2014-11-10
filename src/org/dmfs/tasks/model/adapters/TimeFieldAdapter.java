/*
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

package org.dmfs.tasks.model.adapters;

import java.util.TimeZone;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.format.Time;


/**
 * Knows how to load and store {@link Time} values in a {@link ContentSet}.
 * 
 * Time values are stored as three values:
 * <ul>
 * <li>a timestamp in milliseconds since the epoch</li>
 * <li>a time zone</li>
 * <li>an allday flag</li>
 * </ul>
 * 
 * This adapter combines those three fields in a {@link ContentValues} to a Time value. If the time zone field is <code>null</code> the time zone is always set
 * to UTC.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class TimeFieldAdapter extends FieldAdapter<Time>
{
	private final String mTimestampField;
	private final String mTzField;
	private final String mAllDayField;
	private final boolean mAllDayDefault;


	/**
	 * Constructor for a new TimeFieldAdapter.
	 * 
	 * @param timestampField
	 *            The name of the field that holds the time stamp in milliseconds.
	 * @param tzField
	 *            The name of the field that holds the time zone (as Olson ID). If the field name is <code>null</code> the time is always set to UTC.
	 * @param alldayField
	 *            The name of the field that indicated that this time is a date not a date-time. If this fieldName is <code>null</code> all loaded values are
	 *            non-allday.
	 */
	public TimeFieldAdapter(String timestampField, String tzField, String alldayField)
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
	public Time get(ContentSet values)
	{
		Long timestamp = values.getAsLong(mTimestampField);
		if (timestamp == null)
		{
			// if the time stamp is null we return null
			return null;
		}
		// create a new Time for the given time zone, falling back to UTC if none is given
		String timezone = mTzField == null ? Time.TIMEZONE_UTC : values.getAsString(mTzField);
		Time value = new Time(timezone == null ? Time.TIMEZONE_UTC : timezone);
		// set the time stamp
		value.set(timestamp);

		// cache mAlldayField locally
		String allDayField = mAllDayField;

		// set the allday flag appropriately
		Integer allDayInt = allDayField == null ? null : values.getAsInteger(allDayField);

		if ((allDayInt != null && allDayInt != 0) || (allDayField == null && mAllDayDefault))
		{
			value.set(value.monthDay, value.month, value.year);
			value.timezone = Time.TIMEZONE_UTC;
		}

		return value;
	}


	/**
	 * Method to check if allday flag in values is set. If a <code>null</code> allday field name has been passed to the constructor then return default
	 * {@link TimeFieldAdapter#mAllDayDefault}
	 * 
	 * @param values
	 *            The {@link ContentSet} to get the value of
	 * @return <code>true</code> if allday flag is set, <code>false</code> otherwise
	 */
	public boolean isAllDay(ContentSet values)
	{
		// cache mAlldayField locally
		String allDayField = mAllDayField;

		if (allDayField == null)
		{
			return mAllDayDefault;
		}

		Integer allDayInt = values.getAsInteger(allDayField);

		return (allDayInt != null && allDayInt != 0);

	}


	@Override
	public Time get(Cursor cursor)
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
		String timezone = mTzField == null ? Time.TIMEZONE_UTC : cursor.getString(tzIdx);
		Time value = new Time(timezone == null ? Time.TIMEZONE_UTC : timezone);
		// set the time stamp
		value.set(timestamp);

		// set the allday flag appropriately
		Integer allDayInt = adIdx < 0 ? null : cursor.getInt(adIdx);

		if ((allDayInt != null && allDayInt != 0) || (mAllDayField == null && mAllDayDefault))
		{
			value.set(value.monthDay, value.month, value.year);
		}
		return value;
	}


	@Override
	public Time getDefault(ContentSet values)
	{
		// create a new Time for the given time zone, falling back to the default time zone if none is given
		String timezone = mTzField == null ? Time.TIMEZONE_UTC : values.getAsString(mTzField);
		Time value = new Time(timezone == null ? TimeZone.getDefault().getID() : timezone);

		value.setToNow();

		Integer allDayInt = mAllDayField == null ? null : values.getAsInteger(mAllDayField);
		if ((allDayInt != null && allDayInt != 0) || (mAllDayField == null && mAllDayDefault))
		{
			// make it an allday value
			value.set(value.monthDay, value.month, value.year);
		}
		else
		{
			value.second = 0;
			// round up to next quarter-hour
			value.minute = ((value.minute + 14) / 15) * 15;
			value.normalize(false);
		}

		return value;
	}


	@Override
	public void set(ContentSet values, Time value)
	{
		values.startBulkUpdate();
		try
		{
			if (value != null)
			{
				// just store all three parts separately
				values.put(mTimestampField, value.toMillis(false));

				if (mTzField != null)
				{
					values.put(mTzField, value.allDay ? null : value.timezone);
				}
				if (mAllDayField != null)
				{
					values.put(mAllDayField, value.allDay ? 1 : 0);
				}
			}
			else
			{
				// write timestamp only, other fields may still use allday and timezone
				values.put(mTimestampField, (Long) null);
			}
		}
		finally
		{
			values.finishBulkUpdate();
		}
	}


	@Override
	public void set(ContentValues values, Time value)
	{
		if (value != null)
		{
			// just store all three parts separately
			values.put(mTimestampField, value.toMillis(false));

			if (mTzField != null)
			{
				values.put(mTzField, value.allDay ? null : value.timezone);
			}
			if (mAllDayField != null)
			{
				values.put(mAllDayField, value.allDay ? 1 : 0);
			}
		}
		else
		{
			// write timestamp only, other fields may still use allday and timezone
			values.put(mTimestampField, (Long) null);
		}
	}


	/**
	 * Return whether this adapter reads the time zone field.
	 * 
	 * @return <code>true</code> if this adapter evaluates any time zone field, <code>false</code> otherwise.
	 */
	public boolean hasTimeZoneField()
	{
		return mTzField != null;
	}


	@Override
	public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initalNotification)
	{
		values.addOnChangeListener(listener, mTimestampField, initalNotification);
		if (mTzField != null)
		{
			values.addOnChangeListener(listener, mTzField, initalNotification);
		}
		if (mAllDayField != null)
		{
			values.addOnChangeListener(listener, mAllDayField, initalNotification);
		}
	}


	@Override
	public void unregisterListener(ContentSet values, OnContentChangeListener listener)
	{
		values.removeOnChangeListener(listener, mTimestampField);
		if (mTzField != null)
		{
			values.removeOnChangeListener(listener, mTzField);
		}
		if (mAllDayField != null)
		{
			values.removeOnChangeListener(listener, mAllDayField);
		}
	}
}
