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

import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.adapters.FirstPresent;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;

import static org.dmfs.jems.optional.elementary.Absent.absent;


/**
 * Knows how to load and store an {@link Optional} {@link Long} value in a certain field of a {@link ContentSet}.
 */
public class OptionalLongFieldAdapter extends FieldAdapter<Optional<Long>>
{

    /**
     * The field name this adapter uses to store the values.
     */
    private final String mFieldName;

    /**
     * The default value, if any.
     */
    private final Optional<Long> mDefaultValue;


    /**
     * Constructor for a new IntegerFieldAdapter without default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     */
    public OptionalLongFieldAdapter(String fieldName)
    {
        if (fieldName == null)
        {
            throw new IllegalArgumentException("fieldName must not be null");
        }
        mFieldName = fieldName;
        mDefaultValue = absent();
    }


    /**
     * Constructor for a new IntegerFieldAdapter with default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     * @param defaultValue
     *         The default value.
     */
    public OptionalLongFieldAdapter(String fieldName, Optional<Long> defaultValue)
    {
        if (fieldName == null)
        {
            throw new IllegalArgumentException("fieldName must not be null");
        }
        mFieldName = fieldName;
        mDefaultValue = defaultValue;
    }


    @Override
    public Optional<Long> get(ContentSet values)
    {
        // return the value as Integer
        return new FirstPresent<>(new NullSafe<>(values.getAsLong(mFieldName)), mDefaultValue);
    }


    @Override
    public Optional<Long> get(Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(mFieldName);
        if (columnIdx < 0)
        {
            throw new IllegalArgumentException("The fieldName column missing in cursor.");
        }
        return cursor.isNull(columnIdx) ? mDefaultValue : new Present<>(cursor.getLong(columnIdx));
    }


    @Override
    public Optional<Long> getDefault(ContentSet values)
    {
        return mDefaultValue;
    }


    @Override
    public void set(ContentSet values, Optional<Long> value)
    {
        values.put(mFieldName, value.isPresent() ? value.value() : null);
    }


    @Override
    public void set(ContentValues values, Optional<Long> value)
    {
        values.put(mFieldName, value.isPresent() ? value.value() : null);
    }


    @Override
    public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initalNotification)
    {
        values.addOnChangeListener(listener, mFieldName, initalNotification);
    }


    @Override
    public void unregisterListener(ContentSet values, OnContentChangeListener listener)
    {
        values.removeOnChangeListener(listener, mFieldName);
    }
}
