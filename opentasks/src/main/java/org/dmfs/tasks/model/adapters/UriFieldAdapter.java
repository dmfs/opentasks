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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.utils.ValidatingUri;

import java.net.URISyntaxException;


/**
 * Knows how to load and store {@link Uri} values in a {@link ContentSet}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class UriFieldAdapter extends FieldAdapter<Uri>
{

    private final String mFieldName;

    private final Uri mDefaultValue;


    /**
     * Constructor for a new {@link UriFieldAdapter} without default value.
     *
     * @param uriField
     *         The field name that holds the URI.
     */
    public UriFieldAdapter(String uriField)
    {
        if (uriField == null)
        {
            throw new IllegalArgumentException("uriField must not be null");
        }
        mFieldName = uriField;
        mDefaultValue = null;
    }


    /**
     * Constructor for a new {@link UriFieldAdapter} with default value.
     *
     * @param urlField
     *         The name of the field to use when loading or storing the value.
     * @param defaultValue
     *         The defaultValue.
     */
    public UriFieldAdapter(String urlField, Uri defaultValue)
    {
        if (urlField == null)
        {
            throw new IllegalArgumentException("urlField must not be null");
        }
        mFieldName = urlField;
        mDefaultValue = defaultValue;
    }


    @Nullable
    @Override
    public Uri get(@NonNull ContentSet values)
    {
        try
        {
            return new ValidatingUri(values.getAsString(mFieldName)).value();
        }
        catch (URISyntaxException e)
        {
            return null;
        }
    }


    @Nullable
    @Override
    public Uri get(@NonNull Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(mFieldName);
        if (columnIdx < 0)
        {
            throw new IllegalArgumentException("The urlField column missing in cursor.");
        }
        try
        {
            return new ValidatingUri(cursor.getString(columnIdx)).value();
        }
        catch (URISyntaxException e)
        {
            return null;
        }
    }


    @Nullable
    @Override
    public Uri getDefault(@NonNull ContentSet values)
    {
        return mDefaultValue;
    }


    @Override
    public void set(@NonNull ContentSet values, @Nullable Uri value)
    {
        if (value == null)
        {
            values.put(mFieldName, (String) null);
        }
        else
        {
            values.put(mFieldName, value.toString());
        }
    }


    @Override
    public void set(@NonNull ContentValues values, @Nullable Uri value)
    {
        if (value == null)
        {
            values.putNull(mFieldName);
        }
        else
        {
            values.put(mFieldName, value.toString());
        }
    }


    @Override
    public void registerListener(@NonNull ContentSet values, @NonNull OnContentChangeListener listener, boolean initalNotification)
    {
        values.addOnChangeListener(listener, mFieldName, initalNotification);
    }


    @Override
    public void unregisterListener(@NonNull ContentSet values, @NonNull OnContentChangeListener listener)
    {
        values.removeOnChangeListener(listener, mFieldName);
    }
}
