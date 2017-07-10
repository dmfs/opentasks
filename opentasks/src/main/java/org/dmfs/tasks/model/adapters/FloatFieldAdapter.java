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


/**
 * Knows how to load and store an {@link Float} value in a certain field of a {@link ContentSet}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class FloatFieldAdapter extends FieldAdapter<Float>
{

    /**
     * The field name this adapter uses to store the values.
     */
    private final String mFieldName;

    /**
     * The default value, if any.
     */
    private final Float mDefaultValue;


    /**
     * Constructor for a new FloatFieldAdapter without default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     */
    public FloatFieldAdapter(String fieldName)
    {
        if (fieldName == null)
        {
            throw new IllegalArgumentException("fieldName must not be null");
        }
        mFieldName = fieldName;
        mDefaultValue = null;
    }


    /**
     * Constructor for a new FloatFieldAdapter with default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     * @param defaultValue
     *         The default value.
     */
    public FloatFieldAdapter(String fieldName, Float defaultValue)
    {
        if (fieldName == null)
        {
            throw new IllegalArgumentException("fieldName must not be null");
        }
        mFieldName = fieldName;
        mDefaultValue = defaultValue;
    }


    @Override
    public Float get(ContentSet values)
    {
        // return the value as Float
        return values.getAsFloat(mFieldName);
    }


    @Override
    public Float get(Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(mFieldName);
        if (columnIdx < 0)
        {
            throw new IllegalArgumentException("The fieldName column missing in cursor.");
        }
        return cursor.getFloat(columnIdx);
    }


    @Override
    public Float getDefault(ContentSet values)
    {
        return mDefaultValue;
    }


    @Override
    public void set(ContentSet values, Float value)
    {
        values.put(mFieldName, value);
    }


    @Override
    public void set(ContentValues values, Float value)
    {
        values.put(mFieldName, value);
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
