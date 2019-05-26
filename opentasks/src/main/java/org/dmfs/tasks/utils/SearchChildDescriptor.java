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

import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.groupings.filters.AbstractFilter;


/**
 * Describes how to display the children of the search grouping.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class SearchChildDescriptor extends ExpandableChildDescriptor
{
    private String mAuthority;
    private String mQueryColumn;


    /**
     * Create a new {@link SearchChildDescriptor} using the given values.
     *
     * @param projection
     * @param selection
     * @param sortOrder
     * @param selectionColumns
     */
    public SearchChildDescriptor(String authority, String queryColumn, String[] projection, String selection, String sortOrder, int... selectionColumns)
    {
        super();
        mAuthority = authority;
        mQueryColumn = queryColumn;
        mProjection = projection;
        mSelection = selection;
        mSelectionColumns = selectionColumns;
        mSortOrder = sortOrder;
    }


    /**
     * Get a new {@link CursorLoader} and update it's selection arguments with the values in {@code cursor} as defined by {@code selectionColumns}. Also applies any selection defined by <code>filter</code>.
     *
     * @param context
     *         A {@link Context}.
     * @param cursor
     *         The {@link Cursor} containing the selection.
     * @param filter
     *         An additional {@link AbstractFilter} to apply to the selection of the cursor.
     *
     * @return A new {@link CursorLoader} instance.
     */
    @Override
    public CursorLoader getCursorLoader(Context context, Cursor cursor, AbstractFilter filter)
    {
        CursorLoader cursorLoader = super.getCursorLoader(context, cursor, filter);
        cursorLoader.setUri(Tasks.getSearchUri(mAuthority, cursor.getString(cursor.getColumnIndex(mQueryColumn))));

        return cursorLoader;
    }
}
