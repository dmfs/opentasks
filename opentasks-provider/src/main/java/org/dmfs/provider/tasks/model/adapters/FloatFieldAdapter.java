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


/**
 * Knows how to load and store a {@link Float} value from a {@link Cursor} or {@link ContentValues}.
 *
 * @param <EntityType>
 *         The type of the entity the field belongs to.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class FloatFieldAdapter<EntityType> extends SimpleFieldAdapter<Float, EntityType>
{

    /**
     * The field name this adapter uses to store the values.
     */
    private final String mFieldName;


    /**
     * Constructor for a new {@link FloatFieldAdapter}.
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
    }


    @Override
    String fieldName()
    {
        return mFieldName;
    }


    @Override
    public Float getFrom(ContentValues values)
    {
        return values.getAsFloat(mFieldName);
    }


    @Override
    public Float getFrom(Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(mFieldName);
        if (columnIdx < 0)
        {
            throw new IllegalArgumentException("The column '" + mFieldName + "' is missing in cursor.");
        }
        return cursor.isNull(columnIdx) ? null : cursor.getFloat(columnIdx);
    }


    @Override
    public void setIn(ContentValues values, Float value)
    {
        if (value != null)
        {
            values.put(mFieldName, value);
        }
        else
        {
            values.putNull(mFieldName);
        }
    }

}
