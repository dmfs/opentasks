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

import java.net.URI;
import java.net.URL;


/**
 * Knows how to load and store {@link URL} values from a {@link Cursor} or {@link ContentValues}.
 *
 * @param <EntityType>
 *         The type of the entity the field belongs to.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class UrlFieldAdapter<EntityType> extends SimpleFieldAdapter<URI, EntityType>
{

    private final String mFieldName;


    /**
     * Constructor for a new {@link UrlFieldAdapter}.
     *
     * @param urlField
     *         The field name that holds the URL.
     */
    public UrlFieldAdapter(String urlField)
    {
        if (urlField == null)
        {
            throw new IllegalArgumentException("urlField must not be null");
        }
        mFieldName = urlField;
    }


    @Override
    String fieldName()
    {
        return mFieldName;
    }


    @Override
    public URI getFrom(ContentValues values)
    {
        return values.get(mFieldName) == null ? null : URI.create(values.getAsString(mFieldName));
    }


    @Override
    public URI getFrom(Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(mFieldName);
        if (columnIdx < 0)
        {
            throw new IllegalArgumentException("The column '" + mFieldName + "' is missing in cursor.");
        }

        return cursor.isNull(columnIdx) ? null : URI.create(cursor.getString(columnIdx));
    }


    @Override
    public void setIn(ContentValues values, URI value)
    {
        if (value != null)
        {
            values.put(mFieldName, value.toASCIIString());
        }
        else
        {
            values.putNull(mFieldName);
        }
    }
}
