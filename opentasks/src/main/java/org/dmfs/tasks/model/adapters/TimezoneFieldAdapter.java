/*
 * Copyright 2017 dmfs GmbH
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
 */

package org.dmfs.tasks.model.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;

import java.util.TimeZone;


/**
 * Knows how to load and store {@link TimeZone}s in a certain field of a {@link ContentSet}. The returned time zone is always <code>null</code> for all-day
 * dates.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class TimezoneFieldAdapter extends FieldAdapter<TimeZone>
{

    /**
     * The field name this adapter uses to store the time zone.
     */
    private final String mTzFieldName;

    /**
     * The field name this adapter uses to store the all-day flag.
     */
    private final String mAllDayFieldName;

    /**
     * The name of a field that is used to determine if a time zone is in summer time.
     */
    private final String mReferenceTimeFieldName;


    /**
     * Constructor for a new TimezoneFieldAdapter without default value.
     *
     * @param timezoneFieldName
     *         The name of the field to use when loading or storing the value.
     */
    public TimezoneFieldAdapter(String timezoneFieldName, String alldayFieldName)
    {
        if (timezoneFieldName == null)
        {
            throw new IllegalArgumentException("timezoneFieldName must not be null");
        }
        mTzFieldName = timezoneFieldName;
        mAllDayFieldName = alldayFieldName;
        mReferenceTimeFieldName = null;
    }


    /**
     * Constructor for a new TimezoneFieldAdapter without default value.
     *
     * @param timezoneFieldName
     *         The name of the field to use when loading or storing the value.
     */
    public TimezoneFieldAdapter(String timezoneFieldName, String alldayFieldName, String referenceTimeFieldName)
    {
        if (timezoneFieldName == null)
        {
            throw new IllegalArgumentException("timezoneFieldName must not be null");
        }
        mTzFieldName = timezoneFieldName;
        mAllDayFieldName = alldayFieldName;
        mReferenceTimeFieldName = referenceTimeFieldName;
    }


    @Override
    public TimeZone get(ContentSet values)
    {
        String timezoneId = values.getAsString(mTzFieldName);

        boolean isAllDay = false;

        if (mAllDayFieldName != null)
        {
            Integer allday = values.getAsInteger(mAllDayFieldName);
            isAllDay = allday != null && allday > 0;
        }

        TimeZoneWrapper timeZone = isAllDay ? null : timezoneId == null ? getDefault(null) : new TimeZoneWrapper(timezoneId);
        if (timeZone != null && mReferenceTimeFieldName != null)
        {
            timeZone.setReferenceTimeStamp(values.getAsLong(mReferenceTimeFieldName));
        }
        return timeZone;
    }


    @Override
    public TimeZone get(Cursor cursor)
    {
        int tzColumnIdx = cursor.getColumnIndex(mTzFieldName);

        if (tzColumnIdx < 0)
        {
            throw new IllegalArgumentException("The timezone column is missing in cursor.");
        }

        String timezoneId = cursor.getString(tzColumnIdx);

        boolean isAllDay = false;

        if (mAllDayFieldName != null)
        {
            int allDayColumnIdx = cursor.getColumnIndex(mAllDayFieldName);
            if (allDayColumnIdx < 0)
            {
                throw new IllegalArgumentException("The allday column is missing in cursor.");
            }

            isAllDay = !cursor.isNull(allDayColumnIdx) && cursor.getInt(allDayColumnIdx) > 0;
        }

        TimeZoneWrapper timeZone = isAllDay ? null : timezoneId == null ? getDefault(null) : new TimeZoneWrapper(timezoneId);
        int refTimeCol;
        if (timeZone != null && mReferenceTimeFieldName != null && (refTimeCol = cursor.getColumnIndex(mReferenceTimeFieldName)) >= 0)
        {
            timeZone.setReferenceTimeStamp(cursor.getLong(refTimeCol));
        }
        return timeZone;

    }


    /**
     * Returns whether this is an "all-day timezone".
     *
     * @return <code>true</code> if the cursor points to an all-day date.
     */
    public boolean isAllDay(ContentSet values)
    {
        if (mAllDayFieldName == null)
        {
            return false;
        }

        Integer allday = values.getAsInteger(mAllDayFieldName);
        return allday != null && allday > 0;
    }


    /**
     * Returns whether this is an "all-day timezone".
     *
     * @return <code>true</code> if the cursor points to an all-day date.
     */
    public boolean isAllDay(ContentValues values)
    {
        if (mAllDayFieldName == null)
        {
            return false;
        }

        Integer allday = values.getAsInteger(mAllDayFieldName);
        return allday != null && allday > 0;
    }


    /**
     * Returns whether this is an "all-day timezone".
     *
     * @param cursor
     *         The cursor to read from.
     *
     * @return <code>true</code> if the cursor points to an all-day date.
     */
    public boolean isAllDay(Cursor cursor)
    {
        if (mAllDayFieldName == null)
        {
            return false;
        }

        int allDayColumnIdx = cursor.getColumnIndex(mAllDayFieldName);
        if (allDayColumnIdx < 0)
        {
            throw new IllegalArgumentException("The allday column is missing in cursor.");
        }
        return !cursor.isNull(allDayColumnIdx) && cursor.getInt(allDayColumnIdx) > 0;
    }


    /**
     * Returns the local time zone.
     *
     * @return The current time zone.
     */
    @Override
    public TimeZoneWrapper getDefault(ContentSet values)
    {
        return new TimeZoneWrapper();
    }


    @Override
    public void set(ContentSet values, TimeZone value)
    {
        if (!isAllDay(values))
        {
            if (value != null)
            {
                values.put(mTzFieldName, value.getID());
            }
        }
        else
        {
            values.put(mTzFieldName, (String) null);
        }
    }


    @Override
    public void set(ContentValues values, TimeZone value)
    {
        if (!isAllDay(values))
        {
            if (value != null)
            {
                values.put(mTzFieldName, value.getID());
            }
        }
        else
        {
            values.put(mTzFieldName, (String) null);
        }
    }


    @Override
    public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initalNotification)
    {
        values.addOnChangeListener(listener, mTzFieldName, initalNotification);
        if (mAllDayFieldName != null)
        {
            values.addOnChangeListener(listener, mAllDayFieldName, initalNotification);
        }
        if (mReferenceTimeFieldName != null)
        {
            values.addOnChangeListener(listener, mReferenceTimeFieldName, initalNotification);
        }
    }


    @Override
    public void unregisterListener(ContentSet values, OnContentChangeListener listener)
    {
        values.removeOnChangeListener(listener, mTzFieldName);
        if (mAllDayFieldName != null)
        {
            values.removeOnChangeListener(listener, mAllDayFieldName);
        }
        if (mReferenceTimeFieldName != null)
        {
            values.removeOnChangeListener(listener, mReferenceTimeFieldName);
        }
    }
}
