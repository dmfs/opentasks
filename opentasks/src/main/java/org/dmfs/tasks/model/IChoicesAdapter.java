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


/**
 * An interface to a class that provides a number of choices of any type to the user. Choices must be unique and they are matched by {@link #equals(Object)}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface IChoicesAdapter
{

    /**
     * Get the title of the given object.
     *
     * @param object
     *         An object that is among the choices.
     *
     * @return The title or <code>null</code> if no such object is found among the choices.
     */
    String getTitle(Object object);

    /**
     * Get a {@link Drawable} for the given object.
     *
     * @param object
     *         An object that is among the choices.
     *
     * @return A {@link Drawable} or <code>null</code> if no such object is found among the choices.
     */
    Drawable getDrawable(Object object);

    /**
     * Get the position of the object among the choices.
     *
     * @param object
     *         An object that is among the choices.
     *
     * @return The position of the choice or <code>-1</code> if no such object is found among the choices.
     */
    int getIndex(Object object);

    /**
     * Get the number of choices.
     *
     * @return The number of choices.
     */
    int getCount();

    /**
     * Get the choice at the specified position.
     *
     * @param position
     *         The position.
     *
     * @return The choice object.
     *
     * @throws IndexOutOfBoundsException
     *         if the position is invalid.
     */
    Object getItem(int position);
}
