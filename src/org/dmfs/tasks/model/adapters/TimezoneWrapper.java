package org.dmfs.tasks.model.adapters;

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

import java.util.Date;
import java.util.TimeZone;


/**
 * This is a wrapper for a {@link TimeZone} that provides a less strict {@link #equals(Object)} method than some {@link TimeZone} implementations.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TimezoneWrapper extends TimeZone
{

	/**
	 * Generated serial id.
	 */
	private static final long serialVersionUID = -7166830450275216013L;

	/**
	 * Time stamp of 2013-01-01 00:00:00 UTC.
	 */
	private final static Date CHECK_DATE = new Date(1356998400000L);

	private final TimeZone mTimeZone;


	public TimezoneWrapper()
	{
		mTimeZone = TimeZone.getDefault();
		setID(mTimeZone.getID());
	}


	public TimezoneWrapper(TimeZone timeZone)
	{
		mTimeZone = timeZone;
		setID(timeZone.getID());
	}


	public TimezoneWrapper(String id)
	{
		mTimeZone = TimeZone.getTimeZone(id);
		setID(mTimeZone.getID());
	}


	@Override
	public int getOffset(int era, int year, int month, int day, int dayOfWeek, int timeOfDayMillis)
	{
		return mTimeZone.getOffset(era, year, month, day, dayOfWeek, timeOfDayMillis);
	}


	@Override
	public int getRawOffset()
	{
		return mTimeZone.getRawOffset();
	}


	@Override
	public boolean inDaylightTime(Date time)
	{
		return mTimeZone.inDaylightTime(time);
	}


	@Override
	public void setRawOffset(int offsetMillis)
	{
		mTimeZone.setRawOffset(offsetMillis);
	}


	@Override
	public boolean useDaylightTime()
	{
		return mTimeZone.useDaylightTime();
	}


	@Override
	public int hashCode()
	{
		/*
		 * Return the raw offset as hash. It fulfills the requirements of a hash values: Timezones with that are equal have the same raw offset.
		 */
		return getRawOffset();
	}


	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof TimezoneWrapper)) // matches null too
		{
			return false;
		}

		TimeZone otherTimeZone = (TimeZone) object;

		/*
		 * This is a very simple check for equality of two time zones. It returns the wrong result if two time zones have the same UTC offset, but use different
		 * dates to switch to summer time.
		 * 
		 * Are there such cases?
		 */
		return mTimeZone.useDaylightTime() == otherTimeZone.useDaylightTime() && mTimeZone.getRawOffset() == otherTimeZone.getRawOffset()
			&& mTimeZone.getDSTSavings() == otherTimeZone.getDSTSavings() && mTimeZone.inDaylightTime(CHECK_DATE) == otherTimeZone.inDaylightTime(CHECK_DATE);
	}
}
