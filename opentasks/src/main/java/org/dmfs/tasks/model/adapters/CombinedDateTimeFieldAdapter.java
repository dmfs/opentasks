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

import org.dmfs.opentaskspal.datetime.DateTimeDateTimeFields;
import org.dmfs.opentaskspal.datetime.general.MatchedCurrentDateTime;
import org.dmfs.opentaskspal.datetime.general.OptionalTimeZone;
import org.dmfs.opentaskspal.datetime.general.TimeZones;
import org.dmfs.opentaskspal.datetimefields.DateTimeFields;
import org.dmfs.opentaskspal.readdata.cursor.CursorCombinedDateTime;
import org.dmfs.opentaskspal.readdata.utils.StringBinaryBoolean;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.readdata.ContentSetCombinedDateTime;

import java.util.TimeZone;


/**
 * Knows how to load and store combined date-time values as {@link DateTime} from/to {@link Cursor}, {@link ContentSet}, {@link ContentValues}.
 * <p>
 * Combined date-time values are stored as three values:
 * <ul>
 * <li>a timestamp in milliseconds since the epoch</li>
 * <li>a time zone</li>
 * <li>an all-day flag</li>
 * </ul>
 *
 * @author Gabor Keszthelyi
 */
public final class CombinedDateTimeFieldAdapter extends FieldAdapter<DateTime>
{
    private final String mTimestampField;
    private final String mTimeZoneField;
    private final String mIsAllDayField;


    /**
     * Constructor for a new {@link CombinedDateTimeFieldAdapter}.
     *
     * @param timestampField
     *         The name of the field that holds the time stamp in milliseconds.
     * @param timeZoneField
     *         The name of the field that holds the time zone (as Olson ID).
     * @param isAllDayField
     *         The name of the field that indicated that this time is a date not a date-time.
     */
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


    // TODO @NonNull and @Nullable annotations in this class are all assumptions until #644 is completed.
    // TODO getDefault()'s param may change to @Nullable - may need to update usage
    @Override
    public DateTime getDefault(@NonNull ContentSet values)
    {
        TimeZone timeZone = new OptionalTimeZone(values.getAsString(mTimeZoneField)).value(TimeZones.UTC);
        boolean isAllDay = new StringBinaryBoolean(values.getAsString(mIsAllDayField)).value();
        return new MatchedCurrentDateTime(timeZone, isAllDay).value();
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
        // TODO How to remove semantic code duplication with the above
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
