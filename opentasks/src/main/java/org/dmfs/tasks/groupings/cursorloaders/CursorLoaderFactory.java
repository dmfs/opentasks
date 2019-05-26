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

package org.dmfs.tasks.groupings.cursorloaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;


/**
 * A concrete Factory for a {@link CursorLoader}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class CursorLoaderFactory extends AbstractCursorLoaderFactory
{
    private final Uri mUri;
    private final String[] mProjection;
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final String mSortOrder;


    /**
     * Initialize the Factory with the arguments to initialize the CursorLoader. The parameters are just passed to
     * {@link CursorLoader#CursorLoader(Context, Uri, String[], String, String[], String)}.
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     */
    public CursorLoaderFactory(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }


    @Override
    public Loader<Cursor> getLoader(Context context)
    {
        return new CursorLoader(context, mUri, mProjection, mSelection, mSelectionArgs, mSortOrder);
    }

}
