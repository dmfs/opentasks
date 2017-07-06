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
 * Knows how to load and store a {@link Boolean} value from a {@link Cursor} or {@link ContentValues}.
 * <p/>
 * Implementation detail:
 * <p/>
 * The values are loaded and stored as <code>0</code> (for <code>false</code>) and <code>1</code> (for <code>true</code>).
 *
 * @param <EntityType>
 *         The type of the entity the field belongs to.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class BooleanFieldAdapter<EntityType> extends SimpleFieldAdapter<Boolean, EntityType>
{

    /**
     * The field name this adapter uses to store the values.
     */
    private final String mFieldName;


    /**
     * Constructor for a new {@link BooleanFieldAdapter}.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     */
    public BooleanFieldAdapter(String fieldName)
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
    public Boolean getFrom(ContentValues values)
    {
        Integer value = values.getAsInteger(mFieldName);

        return value != null && value > 0;
    }


    @Override
    public Boolean getFrom(Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(mFieldName);
        if (columnIdx < 0)
        {
            throw new IllegalArgumentException("The column '" + mFieldName + "' is missing in cursor.");
        }
        return !cursor.isNull(columnIdx) && cursor.getInt(columnIdx) > 0;
    }


    @Override
    public void setIn(ContentValues values, Boolean value)
    {
        values.put(mFieldName, value ? 1 : 0);
    }

}
