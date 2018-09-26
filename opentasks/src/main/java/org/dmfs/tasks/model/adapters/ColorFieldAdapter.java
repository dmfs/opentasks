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

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.utils.colors.SmoothLightnessCapped;


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
    public Integer get(ContentSet values)
    {
        return new SmoothLightnessCapped(mDarkenThreshold, super.get(values)).argb();
    }


    @Override
    public Integer get(Cursor cursor)
    {
        return new SmoothLightnessCapped(mDarkenThreshold, super.get(cursor)).argb();
    }


    @Override
    public Integer getDefault(ContentSet values)
    {
        return new SmoothLightnessCapped(mDarkenThreshold, super.getDefault(values)).argb();
    }

}
