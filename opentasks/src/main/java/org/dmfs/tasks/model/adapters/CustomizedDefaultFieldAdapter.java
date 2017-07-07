/*
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
 *
 */

package org.dmfs.tasks.model.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.model.defaults.Default;


/**
 * Enhances an existing {@link FieldAdapter} with a custom default value generator.
 *
 * @param <Type>
 *         Type of the {@link FieldAdapter}
 */
public class CustomizedDefaultFieldAdapter<Type> extends FieldAdapter<Type>
{

    private final FieldAdapter<Type> mFieldAdapter;
    private final Default<Type> mDefault;


    /**
     * Constructor for a new CustomizedDefaultFieldAdapter
     *
     * @param fieldAdapter
     *         FieldAdapter which forms the base for this Adapter.
     * @param defaultGenerator
     *         Custom default value generator.
     */
    public CustomizedDefaultFieldAdapter(FieldAdapter<Type> fieldAdapter, Default<Type> defaultGenerator)
    {
        if (fieldAdapter == null)
        {
            throw new IllegalArgumentException("fieldAdapter must not be null");
        }
        if (defaultGenerator == null)
        {
            throw new IllegalArgumentException("defaultGenerator must not be null");
        }
        this.mFieldAdapter = fieldAdapter;
        this.mDefault = defaultGenerator;
    }


    @Override
    public Type get(ContentSet values)
    {
        return mFieldAdapter.get(values);
    }


    @Override
    public Type get(Cursor cursor)
    {
        return mFieldAdapter.get(cursor);
    }


    /**
     * Get a default value for the {@link FieldAdapter} based on the {@link Default} instance.
     *
     * @param values
     *         The {@link ContentSet}.
     *
     * @return A default Value
     */
    @Override
    public Type getDefault(ContentSet values)
    {
        Type defaultValue = mFieldAdapter.getDefault(values);
        return mDefault.getCustomDefault(values, defaultValue);
    }


    @Override
    public void set(ContentSet values, Type value)
    {
        mFieldAdapter.set(values, value);
    }


    @Override
    public void set(ContentValues values, Type value)
    {
        mFieldAdapter.set(values, value);
    }


    @Override
    public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initialNotification)
    {
        mFieldAdapter.registerListener(values, listener, initialNotification);
    }


    @Override
    public void unregisterListener(ContentSet values, OnContentChangeListener listener)
    {
        mFieldAdapter.unregisterListener(values, listener);
    }
}
