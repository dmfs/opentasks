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

package org.dmfs.provider.tasks.model.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import org.dmfs.iterables.EmptyIterable;
import org.dmfs.iterables.Split;
import org.dmfs.iterables.decorators.DelegatingIterable;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.rfc5545.DateTime;

import java.util.TimeZone;


/**
 * Knows how to load and store {@link Iterable}s of {@link DateTime} values from a {@link Cursor} or {@link ContentValues}.
 *
 * @param <EntityType>
 *         The type of the entity the field belongs to.
 *
 * @author Marten Gajda
 */
public final class DateTimeIterableFieldAdapter<EntityType> extends SimpleFieldAdapter<Iterable<DateTime>, EntityType>
{
    private final String mDateTimeListFieldName;
    private final String mTimeZoneFieldName;


    /**
     * Constructor for a new {@link DateTimeIterableFieldAdapter}.
     *
     * @param datetimeListFieldName
     *         The name of the field that holds the {@link DateTime} list.
     * @param timezoneFieldName
     *         The name of the field that holds the time zone name.
     */
    public DateTimeIterableFieldAdapter(String datetimeListFieldName, String timezoneFieldName)
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
    public Iterable<DateTime> getFrom(ContentValues values)
    {
        String datetimeList = values.getAsString(mDateTimeListFieldName);
        if (datetimeList == null)
        {
            // no list, return an empty Iterable
            return EmptyIterable.instance();
        }

        // create a new TimeZone for the given time zone string
        String timezoneString = mTimeZoneFieldName == null ? null : values.getAsString(mTimeZoneFieldName);
        TimeZone timeZone = timezoneString == null ? null : TimeZone.getTimeZone(timezoneString);

        return new DateTimeList(timeZone, datetimeList);
    }


    @Override
    public Iterable<DateTime> getFrom(Cursor cursor)
    {
        int tdLIdx = cursor.getColumnIndex(mDateTimeListFieldName);
        int tzIdx = mTimeZoneFieldName == null ? -1 : cursor.getColumnIndex(mTimeZoneFieldName);

        if (tdLIdx < 0 || (mTimeZoneFieldName != null && tzIdx < 0))
        {
            throw new IllegalArgumentException("At least one column is missing in cursor.");
        }

        if (cursor.isNull(tdLIdx))
        {
            // if the time stamp list is null we return an empty Iterable
            return EmptyIterable.instance();
        }

        String datetimeList = cursor.getString(tdLIdx);

        // create a new TimeZone for the given time zone string
        String timezoneString = mTimeZoneFieldName == null ? null : cursor.getString(tzIdx);
        TimeZone timeZone = timezoneString == null ? null : TimeZone.getTimeZone(timezoneString);

        return new DateTimeList(timeZone, datetimeList);
    }


    @Override
    public Iterable<DateTime> getFrom(Cursor cursor, ContentValues values)
    {
        int tsIdx;
        int tzIdx;
        String datetimeList;
        String timeZoneId = null;

        if (values != null && values.containsKey(mDateTimeListFieldName))
        {
            if (values.getAsString(mDateTimeListFieldName) == null)
            {
                // the date times are null, so we return null
                return EmptyIterable.instance();
            }
            datetimeList = values.getAsString(mDateTimeListFieldName);
        }
        else if (cursor != null && (tsIdx = cursor.getColumnIndex(mDateTimeListFieldName)) >= 0)
        {
            if (cursor.isNull(tsIdx))
            {
                // the date times are null, so we return an empty Iterable.
                return EmptyIterable.instance();
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

        return new DateTimeList(timeZone, datetimeList);
    }


    @Override
    public void setIn(ContentValues values, Iterable<DateTime> value)
    {
        if (value != null)
        {
            String stringValue = TextUtils.join(",", new Mapped<>(dt -> dt.isFloating() ? dt : dt.shiftTimeZone(DateTime.UTC), value));
            values.put(mDateTimeListFieldName, stringValue.isEmpty() ? null : stringValue);
        }
        else
        {
            values.put(mDateTimeListFieldName, (String) null);
        }
    }


    private final class DateTimeList extends DelegatingIterable<DateTime>
    {

        public DateTimeList(TimeZone timeZone, String dateTimeList)
        {
            super(new Mapped<>(
                    datetime -> !datetime.isFloating() && timeZone != null ? datetime.shiftTimeZone(timeZone) : datetime,
                    new Mapped<CharSequence, DateTime>(
                            charSequence -> DateTime.parse(timeZone, charSequence.toString()),
                            new Split(dateTimeList, ','))));
        }
    }
}
