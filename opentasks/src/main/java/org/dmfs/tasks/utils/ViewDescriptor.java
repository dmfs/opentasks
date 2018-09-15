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

import android.database.Cursor;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;


/**
 * The interface to a class that knows how to populate a view in an {@link ExpandableListView}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public interface ViewDescriptor
{
    /**
     * Flag for {@link #populateView(View, Cursor, BaseExpandableListAdapter, int)} that indicates the view is the last child in a group.
     */
    int FLAG_IS_LAST_CHILD = 0x0001;

    /**
     * Flag for {@link #populateView(View, Cursor, BaseExpandableListAdapter, int)} that indicates the view is group that's currently expanded.
     */
    int FLAG_IS_EXPANDED = 0x0002;

    /**
     * Populate a view in an {@link ExpandableListView}.
     *
     * @param view
     *         The {@link View} to populate.
     * @param cursor
     *         A {@link Cursor} that points to the current data item.
     * @param adapter
     *         The {@link BaseExpandableListAdapter}.
     * @param flags
     *         Some flags that give additional information about the view. Any combination of {@link #FLAG_IS_EXPANDED} or {@link #FLAG_IS_LAST_CHILD}.
     */
    void populateView(View view, Cursor cursor, BaseExpandableListAdapter adapter, int flags);

    /**
     * Get the resource id of the view to use.
     *
     * @return The id of a layout resource.
     */
    int getView();

    /**
     * Get the id of the inner content view that is supposed to fling
     *
     * @return The id of the view (-1) if the view is missing
     */
    int getFlingContentViewId();

    /**
     * Get the id of the view that reveals from the left side
     *
     * @return The id of the view (-1) if the view is missing
     */
    int getFlingRevealLeftViewId();

    /**
     * Get the id of the view that reveals from the right side
     *
     * @return The id of the view (-1) if the view is missing
     */
    int getFlingRevealRightViewId();
}
