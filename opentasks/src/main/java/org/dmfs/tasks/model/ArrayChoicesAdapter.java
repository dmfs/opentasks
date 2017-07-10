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

import android.graphics.drawable.Drawable;

import java.util.ArrayList;


/**
 * Used for creating a generic array type adapter. Supports adding choices of types regular and hidden into the adapter.
 *
 * @author Arjun Naik<arjun@arjunnaik.in>
 * @author Marten Gajda<marten@dmfs.org>
 */
public class ArrayChoicesAdapter extends AbstractArrayChoicesAdapter
{

    public ArrayChoicesAdapter()
    {
        mChoices = new ArrayList<Object>();
        mDrawables = new ArrayList<Drawable>();
        mTitles = new ArrayList<String>();
        mVisibleChoices = new ArrayList<Object>();
    }


    /**
     * Adds a choice which is visible.
     *
     * @param choice
     *         Choice to be adde3d
     * @param title
     *         Title of the choice
     * @param drawable
     *         {@link Drawable} used to display choice
     *
     * @return itself as a reference so that it can used for chaining.
     */
    public ArrayChoicesAdapter addChoice(Object choice, String title, Drawable drawable)
    {
        mVisibleChoices.add(choice);
        mChoices.add(choice);
        mTitles.add(title);
        mDrawables.add(drawable);
        return this;
    }


    /**
     * Add a choice which is hidden.
     *
     * @param choice
     *         Choice to be adde3d
     * @param title
     *         Title of the choice
     * @param drawable
     *         {@link Drawable} used to display choice
     *
     * @return itself as a reference so that it can used for chaining.
     */
    public ArrayChoicesAdapter addHiddenChoice(Object choice, String title, Drawable drawable)
    {
        mChoices.add(choice);
        mTitles.add(title);
        mDrawables.add(drawable);
        return this;
    }

}
