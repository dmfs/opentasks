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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.opentaskspal.datetime.CombinedDateTime;
import org.dmfs.opentaskspal.datetime.general.OptionalTimeZone;
import org.dmfs.opentaskspal.datetimefields.DateTimeFields;
import org.dmfs.opentaskspal.datetimefields.adapters.DateTimeDateTimeFields;
import org.dmfs.opentaskspal.readdata.cursor.CursorCombinedDateTime;
import org.dmfs.opentaskspal.utils.binarybooleans.BinaryLongBoolean;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.model.datetime.ContentSetCombinedDateTime;
import org.dmfs.tasks.model.datetime.ContentSetDateTimeFields;


/**
 * Knows how to load and store combined date-time values as {@link DateTime} from/to {@link Cursor}, {@link ContentSet}, {@link ContentValues}.
 * <p>
 * Combined date-time values are stored as three values: timestamp, time zone, all-day flag
 *
 * @author Gabor Keszthelyi
 */
public final class CombinedDateTimeFieldAdapter extends FieldAdapter<DateTime>
{
    private final String mTimestampField;
    private final String mTimeZoneField;
    private final String mIsAllDayField;


    public CombinedDateTimeFieldAdapter(@NonNull String timestampField,
                                        @NonNull String timeZoneField,
                                        @NonNull String isAllDayField)
    {
        mTimestampField = timestampField;
        mTimeZoneField = timeZoneField;
        mIsAllDayField = isAllDayField;
    }


    @Override
    public DateTime get(@NonNull ContentSet values)
    {
        return new ContentSetCombinedDateTime(values, mTimestampField, mTimeZoneField, mIsAllDayField).value(null);
    }


    @Override
    public DateTime get(@NonNull Cursor cursor)
    {
        return new CursorCombinedDateTime(cursor, mTimestampField, mTimeZoneField, mIsAllDayField).value(null);
    }


    /**
     * Provides a default value for 'now' that respects the time zone and all-day values if they've been already selected
     * (i.e. they are present in the provided {@link ContentSet}.
     * <p>
     * <li>If all-day flag is true, an all-day {@link DateTime} for today is returned.</li>
     * <li>If time zone is provided, a {@link DateTime} for the current time instance with that time zone is returned</li>
     * <li>If time zone is not provided, a floating {@link DateTime} for the current time instance is returned</li>
     * <p>
     * Note: it is up to the presentation layer to apply local time zone when needed
     */
    @Override
    public DateTime getDefault(@NonNull ContentSet values)
    {
        DateTimeFields fields = new ContentSetDateTimeFields(values, mTimestampField, mTimeZoneField, mIsAllDayField);
        return new CombinedDateTime(System.currentTimeMillis(),
                new OptionalTimeZone(fields.timeZoneId()),
                new BinaryLongBoolean(fields.isAllDay())).value();
    }


    @Override
    public void set(@NonNull ContentSet values, @Nullable DateTime dateTime)
    {
        values.bulkUpdate(contentSet ->
        {
            if (dateTime != null)
            {
                DateTimeFields dateTimeFields = new DateTimeDateTimeFields(dateTime);
                values.put(mTimestampField, dateTimeFields.timestamp());
                values.put(mTimeZoneField, dateTimeFields.timeZoneId());
                values.put(mIsAllDayField, dateTimeFields.isAllDay());
            }
            else
            {
                // write timestamp only, other fields may still use all-day and timezone
                values.put(mTimestampField, (Long) null);
            }
        });
    }


    @Override
    public void set(@NonNull ContentValues values, @Nullable DateTime dateTime)
    {
        // TODO Remove semantic code duplication with the above
        // Update<T> type with isNewValue(), isClear(), isNoChange(), T newValue() might help here
        if (dateTime != null)
        {
            DateTimeFields dateTimeFields = new DateTimeDateTimeFields(dateTime);
            values.put(mTimestampField, dateTimeFields.timestamp());
            values.put(mTimeZoneField, dateTimeFields.timeZoneId());
            values.put(mIsAllDayField, dateTimeFields.isAllDay());
        }
        else
        {
            // write timestamp only, other fields may still use all-day and timezone
            values.put(mTimestampField, (Long) null);
        }
    }


    @Override
    public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initalNotification)
    {
        values.addOnChangeListener(listener, mTimestampField, initalNotification);
        values.addOnChangeListener(listener, mTimeZoneField, initalNotification);
        values.addOnChangeListener(listener, mIsAllDayField, initalNotification);
    }


    @Override
    public void unregisterListener(ContentSet values, OnContentChangeListener listener)
    {
        values.removeOnChangeListener(listener, mTimestampField);
        values.removeOnChangeListener(listener, mTimeZoneField);
        values.removeOnChangeListener(listener, mIsAllDayField);
    }
}
