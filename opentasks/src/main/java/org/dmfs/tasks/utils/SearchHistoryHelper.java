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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.tasks.utils.SearchHistoryDatabaseHelper.SearchHistoryColumns;


/**
 * Helper to access the search history.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class SearchHistoryHelper
{
    /**
     * The search history database.
     */
    private final SQLiteDatabase mDb;


    /**
     * Creates a new {@link SearchHistoryHelper}.
     *
     * @param context
     *         A {@link Context}.
     */
    public SearchHistoryHelper(Context context)
    {
        SearchHistoryDatabaseHelper databaseHelper = new SearchHistoryDatabaseHelper(context);
        mDb = databaseHelper.getWritableDatabase();
    }


    /**
     * Returns a {@link Cursor} that contains the search history with the most recent search first.
     *
     * @return A {@link Cursor}.
     */
    public Cursor getSearchHistory()
    {
        return mDb.query(SearchHistoryDatabaseHelper.SEARCH_HISTORY_TABLE, null, null, null, null, null, SearchHistoryColumns._ID + " desc");
    }


    /**
     * Update the current search entry, creating one if only historic search entries exist.
     *
     * @param query
     *         The search query.
     */
    public void updateSearch(String query)
    {
        ContentValues values = new ContentValues(1);
        values.put(SearchHistoryDatabaseHelper.SearchHistoryColumns.SEARCH_QUERY, query);
        values.put(SearchHistoryDatabaseHelper.SearchHistoryColumns.TIMESTAMP, System.currentTimeMillis());
        if (mDb.update(SearchHistoryDatabaseHelper.SEARCH_HISTORY_TABLE, values, SearchHistoryColumns.HISTORIC + "=0", null) == 0)
        {
            mDb.insert(SearchHistoryDatabaseHelper.SEARCH_HISTORY_TABLE, "", values);
        }
    }


    /**
     * Commit the current search, if any, making it a historic search entry.
     */
    public void commitSearch()
    {
        ContentValues values = new ContentValues(1);
        values.put(SearchHistoryDatabaseHelper.SearchHistoryColumns.HISTORIC, 1);
        mDb.update(SearchHistoryDatabaseHelper.SEARCH_HISTORY_TABLE, values, SearchHistoryColumns.HISTORIC + "=0", null);
    }


    /**
     * Remove the current search entry, if any.
     */
    public void removeCurrentSearch()
    {
        mDb.delete(SearchHistoryDatabaseHelper.SEARCH_HISTORY_TABLE, SearchHistoryColumns.HISTORIC + "=0", null);
    }


    /**
     * Remove a specific search entry.
     */
    public void removeSearch(long id)
    {
        mDb.delete(SearchHistoryDatabaseHelper.SEARCH_HISTORY_TABLE, SearchHistoryColumns._ID + "=" + id, null);
    }


    /**
     * Close the database connection.
     */
    public void close()
    {
        mDb.close();
    }
}
