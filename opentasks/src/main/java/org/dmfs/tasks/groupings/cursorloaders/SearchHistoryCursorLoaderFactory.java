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
import androidx.loader.content.Loader;

import org.dmfs.tasks.utils.SearchHistoryHelper;


/**
 * An {@link AbstractCursorLoaderFactory} that returns {@link CursorLoaderFactory} that know how to load cursors with the search history.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class SearchHistoryCursorLoaderFactory extends AbstractCursorLoaderFactory
{

    private final SearchHistoryHelper mSeachHistory;
    private CustomCursorLoader mLastLoader;


    public SearchHistoryCursorLoaderFactory(SearchHistoryHelper history)
    {
        mSeachHistory = history;
    }


    @Override
    public Loader<Cursor> getLoader(Context context)
    {
        return mLastLoader = new CustomCursorLoader(context, new SearchHistoryCursorFactory(context, null, mSeachHistory));

    }


    /**
     * Trigger an update for the last loader that has been created.
     */
    public void forceUpdate()
    {
        if (mLastLoader != null && !mLastLoader.isAbandoned())
        {
            mLastLoader.forceLoad();
        }
    }
}
