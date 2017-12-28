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

import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;


/**
 * Knows how to load and store a 'simple' {@link DateTime} value, i.e. one that doesn't use timezone and all-day flag, just timestamp.
 *
 * @author Gabor Keszthelyi
 */
public final class SimpleDateTimeFieldAdapter extends FieldAdapter<DateTime>
{
    private final String mTimestampField;


    /**
     * Constructor for a new {@link SimpleDateTimeFieldAdapter}.
     *
     * @param timestampField
     *         The name of the field that holds the time stamp in milliseconds.
     */
    public SimpleDateTimeFieldAdapter(@NonNull String timestampField)
    {
        mTimestampField = timestampField;
    }


    @Override
    public DateTime get(ContentSet values)
    {
        Long timeStamp = values.getAsLong(mTimestampField);
        return timeStamp == null ? null : new DateTime(timeStamp);
    }


    @Override
    public DateTime get(Cursor cursor)
    {
        long timeStamp = cursor.getLong(cursor.getColumnIndexOrThrow(mTimestampField));
        // TODO What is tha value of it when it's null or empty?
        return timeStamp > 0 ? new DateTime(timeStamp) : null;
    }


    @Override
    public DateTime getDefault(ContentSet values)
    {
        return DateTime.now();
    }


    @Override
    public void set(ContentSet values, DateTime value)
    {
        values.startBulkUpdate();
        try
        {
            values.put(mTimestampField, value == null ? null : value.getTimestamp());
        }
        finally
        {
            values.finishBulkUpdate();
        }
    }


    @Override
    public void set(ContentValues values, DateTime value)
    {
        values.put(mTimestampField, value == null ? null : value.getTimestamp());
    }


    @Override
    public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initalNotification)
    {
        values.addOnChangeListener(listener, mTimestampField, initalNotification);
    }


    @Override
    public void unregisterListener(ContentSet values, OnContentChangeListener listener)
    {
        values.removeOnChangeListener(listener, mTimestampField);
    }
}
