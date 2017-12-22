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

import org.dmfs.android.bolts.color.Color;
import org.dmfs.android.bolts.color.elementary.ValueColor;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;


/**
 * This extends {@link IntegerFieldAdapter} by an option to darken bright colors when loading them.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
// TODO Check if darkening can be made a decorator, using a function. Could be general Mapped FieldAdapter as well.
// TODO Carefully review the null/0/-1 possibilities, at usage site as well. How to handle them now. TextView.setTextColor for example allows -1, no crash.
public final class ColorFieldAdapter extends FieldAdapter<Color>
{

    private final String mFieldName;
    private final Float mDarkenThreshold;


    public ColorFieldAdapter(String fieldName)
    {
        this(fieldName, 1f);
    }


    public ColorFieldAdapter(String fieldName, float darkenThreshold)
    {
        mFieldName = fieldName;
        mDarkenThreshold = darkenThreshold;
    }


    @Override
    public Color get(ContentSet values)
    {
        return new Darkened(mDarkenThreshold, new ValueColor(values.getAsInteger(mFieldName)));
    }


    @Override
    public Color get(Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(mFieldName);
        if (columnIdx < 0)
        {
            throw new IllegalArgumentException("Column is missing in the cursor: " + mFieldName);
        }
        return new Darkened(mDarkenThreshold, new ValueColor(cursor.getInt(columnIdx)));
    }


    @Override
    public Color getDefault(ContentSet values)
    {
        throw new UnsupportedOperationException("Default value is not defined for " + getClass().getName());
    }


    @Override
    public void set(ContentSet values, Color value)
    {
        values.put(mFieldName, value.argb());
    }


    @Override
    public void set(ContentValues values, Color value)
    {
        values.put(mFieldName, value.argb());
    }


    @Override
    public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initialNotification)
    {
        values.addOnChangeListener(listener, mFieldName, initialNotification);
    }


    @Override
    public void unregisterListener(ContentSet values, OnContentChangeListener listener)
    {
        values.removeOnChangeListener(listener, mFieldName);
    }


    // TODO When #522 is merged, use version added there
    private static final class Darkened implements Color
    {
        private final float mMaxLuminance;
        private final Color mOriginal;


        private Darkened(float maxLuminance, Color original)
        {
            mMaxLuminance = maxLuminance;
            mOriginal = original;
        }


        @Override
        public int argb()
        {
            float[] hsv = new float[3];
            android.graphics.Color.colorToHSV(mOriginal.argb(), hsv);
            hsv[2] = hsv[2] * hsv[2] * hsv[2] * hsv[2] * hsv[2] * (mMaxLuminance - 1) + hsv[2];
            return android.graphics.Color.HSVToColor(hsv);
        }
    }

}
