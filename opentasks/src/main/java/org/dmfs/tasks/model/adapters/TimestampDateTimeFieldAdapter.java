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

import org.dmfs.opentaskspal.datetime.general.OptionalDateTimeTimestamp;
import org.dmfs.opentaskspal.datetime.general.OptionalTimestampDateTime;
import org.dmfs.opentaskspal.readdata.cursor.LongCursorColumnValue;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;


/**
 * Knows how to load and store a {@link DateTime} value that doesn't use timezone and the all-day flag, just the timestamp.
 *
 * @author Gabor Keszthelyi
 */
public final class TimestampDateTimeFieldAdapter extends FieldAdapter<DateTime>
{
    private final String mTimestampField;


    public TimestampDateTimeFieldAdapter(@NonNull String timestampField)
    {
        mTimestampField = timestampField;
    }


    @Override
    public DateTime get(@NonNull ContentSet values)
    {
        return new OptionalTimestampDateTime(values.getAsLong(mTimestampField)).value(null);
    }


    @Override
    public DateTime get(@NonNull Cursor cursor)
    {
        return new OptionalTimestampDateTime(new LongCursorColumnValue(cursor, mTimestampField)).value(null);
    }


    @Override
    public DateTime getDefault(@NonNull ContentSet values)
    {
        return DateTime.now();
    }


    @Override
    public void set(@NonNull ContentSet values, @Nullable DateTime value)
    {
        values.bulkUpdate(contentSet -> contentSet.put(mTimestampField, new OptionalDateTimeTimestamp(value).value(null)));
    }


    @Override
    public void set(@NonNull ContentValues values, @Nullable DateTime value)
    {
        values.put(mTimestampField, new OptionalDateTimeTimestamp(value).value(null));
    }


    @Override
    public void registerListener(@NonNull ContentSet values, @NonNull OnContentChangeListener listener, boolean initialNotification)
    {
        values.addOnChangeListener(listener, mTimestampField, initialNotification);
    }


    @Override
    public void unregisterListener(@NonNull ContentSet values, @NonNull OnContentChangeListener listener)
    {
        values.removeOnChangeListener(listener, mTimestampField);
    }
}
