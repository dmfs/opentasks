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
 * An abstract {@link FieldAdapter} that implements a couple of methods as used by most simple FieldAdapters.
 *
 * @param <FieldType>
 *         The Type of the field this adapter handles.
 * @param <EntityType>
 *         The type of the entity the field belongs to.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class SimpleFieldAdapter<FieldType, EntityType> implements FieldAdapter<FieldType, EntityType>
{

    /**
     * Returns the sole field name of this adapter.
     *
     * @return
     */
    abstract String fieldName();


    @Override
    public boolean existsIn(ContentValues values)
    {
        return values.get(fieldName()) != null;
    }


    @Override
    public boolean isSetIn(ContentValues values)
    {
        return values.containsKey(fieldName());
    }


    @Override
    public boolean existsIn(Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(fieldName());
        return columnIdx >= 0 && !cursor.isNull(columnIdx);
    }


    @Override
    public FieldType getFrom(Cursor cursor, ContentValues values)
    {
        return values.containsKey(fieldName()) ? getFrom(values) : getFrom(cursor);
    }


    @Override
    public boolean existsIn(Cursor cursor, ContentValues values)
    {
        return existsIn(values) || existsIn(cursor);
    }


    @Override
    public void removeFrom(ContentValues values)
    {
        values.remove(fieldName());
    }


    @Override
    public void copyValue(Cursor cursor, ContentValues values)
    {
        setIn(values, getFrom(cursor));
    }


    @Override
    public void copyValue(ContentValues oldValues, ContentValues newValues)
    {
        setIn(newValues, getFrom(oldValues));
    }

}
