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

import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.tasks.model.ContentSet;


/**
 * This extends {@link IntegerFieldAdapter} by an option to darken bright colors when loading them.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ColorFieldAdapter extends IntegerFieldAdapter
{

    private final Float mDarkenThreshold;


    /**
     * Constructor for a new IntegerFieldAdapter without default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     */
    public ColorFieldAdapter(String fieldName)
    {
        this(fieldName, 1f);
    }


    /**
     * Constructor for a new IntegerFieldAdapter without default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     */
    public ColorFieldAdapter(String fieldName, float darkenThreshold)
    {
        super(fieldName);
        mDarkenThreshold = darkenThreshold;
    }


    /**
     * Constructor for a new IntegerFieldAdapter with default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     * @param defaultValue
     *         The default value.
     */
    public ColorFieldAdapter(String fieldName, Integer defaultValue)
    {
        this(fieldName, defaultValue, 1f);
    }


    /**
     * Constructor for a new IntegerFieldAdapter with default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     * @param defaultValue
     *         The default value.
     */
    public ColorFieldAdapter(String fieldName, Integer defaultValue, float darkenThreshold)
    {
        super(fieldName, defaultValue);
        mDarkenThreshold = darkenThreshold;
    }


    @Override
    public Integer get(@NonNull ContentSet values)
    {
        return darkenColor(super.get(values), mDarkenThreshold);
    }


    @Override
    public Integer get(@NonNull Cursor cursor)
    {
        return darkenColor(super.get(cursor), mDarkenThreshold);
    }


    @Override
    public Integer getDefault(@NonNull ContentSet values)
    {
        return darkenColor(super.getDefault(values), mDarkenThreshold);
    }


    @ColorInt
    private static Integer darkenColor(@Nullable Integer color, float maxLuminance)
    {
        if (color == null)
        {
            return null;
        }

        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = hsv[2] * hsv[2] * hsv[2] * hsv[2] * hsv[2] * (maxLuminance - 1) + hsv[2];
        color = Color.HSVToColor(hsv);
        return color;
    }

}
