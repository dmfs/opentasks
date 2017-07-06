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

package org.dmfs.tasks.model;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * ArrayAdapter which loads the Array elements from a resource.
 *
 * @author Arjun Naik<arjun@arjunnaik.in>
 */
public class ResourceArrayChoicesAdapter extends AbstractArrayChoicesAdapter
{

    /**
     * Constructor uses the resource id and the context to load the array
     *
     * @param valuesResource
     *         The resource id of the array
     * @param context
     *         The context
     */
    public ResourceArrayChoicesAdapter(int valuesResource, Context context)
    {
        mChoices = mVisibleChoices = new ArrayList<Object>(Arrays.asList(context.getResources().getStringArray(valuesResource)));
    }


    /**
     * Constructor uses the resource id of the values array and the titles array and the context to load the array
     *
     * @param valuesResource
     *         The resource id of the values array
     * @param titlesResource
     *         The resource id of the titles array
     * @param context
     *         The context
     */
    public ResourceArrayChoicesAdapter(int valuesResource, int titlesResource, Context context)
    {
        Resources resources = context.getResources();
        mTitles = Arrays.asList(resources.getStringArray(titlesResource));
        mChoices = mVisibleChoices = new ArrayList<Object>(Arrays.asList(resources.getStringArray(valuesResource)));
    }


    /**
     * Constructor uses the resource id of the values array and the titles array and the context to load the array
     *
     * @param valuesResource
     *         The resource id of the values array
     * @param titlesResource
     *         The resource id of the titles array
     * @param drawablesResource
     *         The resource id of the drawables array
     * @param context
     *         The context
     */
    public ResourceArrayChoicesAdapter(int valuesResource, int titlesResource, int drawablesResource, Context context)
    {
        Resources resources = context.getResources();
        mTitles = Arrays.asList(resources.getStringArray(titlesResource));
        mChoices = mVisibleChoices = new ArrayList<Object>(Arrays.asList(resources.getStringArray(valuesResource)));
        TypedArray drawables = resources.obtainTypedArray(drawablesResource);
        mDrawables = new ArrayList<Drawable>();
        for (int i = 0; i < drawables.length(); i++)
        {
            mDrawables.add(drawables.getDrawable(i));
        }
        drawables.recycle();
    }

}
