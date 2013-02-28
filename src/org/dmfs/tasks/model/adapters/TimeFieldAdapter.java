/*
 * TimeFieldAdapter.java
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

package org.dmfs.tasks.model.adapters;

import java.util.TimeZone;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.format.Time;


/**
 * A TimeFieldAdapter stores {@link Time} values in {@link ContentValues}.
 * 
 * Time values are stored in three parts:
 * <ul>
 * <li>a timestamp in milliseconds since the epoch</li>
 * <li>a time zone</li>
 * <li>an allday flag</li>
 * </ul>
 * 
 * This adapter combines those three fields in a {@link ContentValues} to a Time value. If the time zone field is <code>null</code> the time zone is always read
 * or written as UTC.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class TimeFieldAdapter extends FieldAdapter<Time>
{
	private final String mTimestampField;
	private final String mTzField;
	private final String mAllDayField;


	/**
	 * Constructor for a new TimeFieldAdapter.
	 * 
	 * @param timestampField
	 *            The field name that holds the time stamp in milliseconds.
	 * @param tzField
	 *            The field that holds the time zone (as Olson ID). Can be <code>null</code> to indicate the time is always loaded and stored in UTC.
	 * @param alldayField
	 *            The Field that indicated that this time is a day not a date-time.
	 */
	public TimeFieldAdapter(String timestampField, String tzField, String alldayField)
	{
		mTimestampField = timestampField;
		mTzField = tzField;
		mAllDayField = alldayField;
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

		// set the allday flag appropriately
		Integer allDayInt = values.getAsInteger(mAllDayField);

		value.allDay = allDayInt != null && allDayInt != 0;
		return value;
	}


	@Override
	public Time get(Cursor cursor)
	{
		int tsIdx = cursor.getColumnIndex(mTimestampField);
		int tzIdx = mTzField == null ? -1 : cursor.getColumnIndex(mTzField);
		int adIdx = cursor.getColumnIndex(mAllDayField);

		if (tsIdx < 0 || (mTzField != null && tzIdx < 0) || adIdx < 0)
		{
			return null;
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
		Integer allDayInt = cursor.getInt(adIdx);

		value.allDay = allDayInt != null && allDayInt != 0;
		return value;
	}


	@Override
	public Time getDefault(ContentSet values)
	{
		// create a new Time for the given time zone, falling back to the default time zone if none is given
		String timezone = mTzField == null ? Time.TIMEZONE_UTC : values.getAsString(mTzField);
		Time value = new Time(timezone == null ? TimeZone.getDefault().getID() : timezone);

		value.setToNow();

		Integer allDayInt = values.getAsInteger(mAllDayField);
		if (allDayInt != null && allDayInt != 0)
		{
			value.set(value.year, value.month, value.monthDay);
		}
		else
		{
			value.second = 0;
			// round up to next quarter
			value.minute = ((value.minute + 14) / 15) * 15;
			value.normalize(false);
		}

		return value;
	}


	@Override
	public void set(ContentSet values, Time value)
	{
		if (value != null)
		{
			// just store all three parts separately
			values.put(mTimestampField, value.toMillis(false));
			if (mTzField != null)
			{
				values.put(mTzField, value.timezone);
			}
			values.put(mAllDayField, value.allDay ? 1 : 0);
		}
		else
		{
			// write timestamp only, other fields may still use allday and timezone
			values.put(mTimestampField, (Long) null);
		}
	}


	@Override
	public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initalNotification)
	{
		values.addOnChangeListener(listener, mTimestampField, initalNotification);
		if (mTzField != null)
		{
			values.addOnChangeListener(listener, mTzField, initalNotification);
		}
		values.addOnChangeListener(listener, mAllDayField, initalNotification);
	}


	@Override
	public void unregisterListener(ContentSet values, OnContentChangeListener listener)
	{
		values.removeOnChangeListener(listener, mTimestampField);
		if (mTzField != null)
		{
			values.removeOnChangeListener(listener, mTzField);
		}
		values.removeOnChangeListener(listener, mAllDayField);
	}
}
