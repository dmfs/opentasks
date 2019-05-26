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

package org.dmfs.tasks.utils;

import android.content.Context;
import android.database.Cursor;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import org.dmfs.tasks.groupings.cursorloaders.AbstractCursorLoaderFactory;
import org.dmfs.tasks.groupings.filters.AbstractFilter;


/**
 * A descriptor that knows how to load and present grouped data.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ExpandableGroupDescriptor
{
    private final AbstractCursorLoaderFactory mLoaderFactory;
    private final ExpandableChildDescriptor mChildDescriptor;
    private ViewDescriptor mGroupViewDescriptor;


    /**
     * Create a new descriptor for expandable groups.
     *
     * @param loaderFactory
     *         An {@link AbstractCursorLoaderFactory} instance that can return {@link CursorLoader}s that load the groups.
     * @param childDescriptor
     *         An {@link ExpandableChildDescriptor} that knwos how to load the children of the groups.
     */
    public ExpandableGroupDescriptor(AbstractCursorLoaderFactory loaderFactory, ExpandableChildDescriptor childDescriptor)
    {
        mLoaderFactory = loaderFactory;
        mChildDescriptor = childDescriptor;
    }


    /**
     * Get a {@link Loader} that loads the groups.
     *
     * @param context
     *         A {@link Context}.
     *
     * @return A {@link Loader}.
     */
    public Loader<Cursor> getGroupCursorLoader(Context context)
    {
        return mLoaderFactory.getLoader(context);
    }


    /**
     * Get a {@link Loader} that loads the children of the group at the current position in a {@link Cursor}.
     *
     * @param context
     *         A {@link Context}.
     * @param cursor
     *         A {@link Cursor} that points to the group to load.
     *
     * @return A {@link Loader}.
     */
    public Loader<Cursor> getChildCursorLoader(Context context, Cursor cursor)
    {
        return mChildDescriptor.getCursorLoader(context, cursor);
    }


    /**
     * Get a {@link Loader} that loads the children of the group at the current position in a {@link Cursor}.
     *
     * @param context
     *         A {@link Context}.
     * @param cursor
     *         A {@link Cursor} that points to the group to load.
     * @param filter
     *         An additional filter to filter the children.
     *
     * @return A {@link Loader}.
     */
    public Loader<Cursor> getChildCursorLoader(Context context, Cursor cursor, AbstractFilter filter)
    {
        return mChildDescriptor.getCursorLoader(context, cursor, filter);
    }


    /**
     * Set the {@link ViewDescriptor} that knows how to populate the group views.
     *
     * @param descriptor
     *         The {@link ViewDescriptor} for the group headers.
     *
     * @return This instance.
     */
    public ExpandableGroupDescriptor setViewDescriptor(ViewDescriptor descriptor)
    {
        mGroupViewDescriptor = descriptor;
        return this;
    }


    /**
     * Get the {@link ViewDescriptor} that knows how to populate the group views.
     *
     * @return A {@link ViewDescriptor}.
     */
    public ViewDescriptor getGroupViewDescriptor()
    {
        return mGroupViewDescriptor;
    }


    /**
     * Get the {@link ViewDescriptor} that knows how to populate the child views.
     *
     * @return A {@link ViewDescriptor}.
     */
    public ViewDescriptor getElementViewDescriptor()
    {
        return mChildDescriptor.getViewDescriptor();
    }

}
