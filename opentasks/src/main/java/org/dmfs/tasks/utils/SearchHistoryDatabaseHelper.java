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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Maintains the search history in a databse.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class SearchHistoryDatabaseHelper extends SQLiteOpenHelper
{

    /**
     * Database schema version number.
     */
    private final static int VERSION = 1;

    /**
     * Number of old entries to keep.
     */
    private final static int SEARCH_HISTORY_SIZE = 10;

    /**
     * Name of the database.
     */
    private final static String SEARCH_HISTORY_DATABASE = "org.dmfs.tasks.search_history.db";


    /**
     * Columns of the search history table.
     */
    public interface SearchHistoryColumns
    {
        /**
         * The row id.
         */
        String _ID = "_id";

        /**
         * The search query string.
         */
        String SEARCH_QUERY = "query";

        /**
         * Flag for historic search entries.
         */
        String HISTORIC = "historic";

        /**
         * Time since the epoch in milliseconds of when the item was updated the last time.
         */
        String TIMESTAMP = "timestamp";
    }


    /**
     * The table name.
     */
    static final String SEARCH_HISTORY_TABLE = "search_history";

    // @formatter:off
    private final static String SQL_CREATE_SEARCH_HISTORY_TABLE =
        "CREATE TABLE " + SEARCH_HISTORY_TABLE + " ( "
            + SearchHistoryColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SearchHistoryColumns.SEARCH_QUERY + " TEXT, "
            + SearchHistoryColumns.HISTORIC + " INTEGER DEFAULT 0,"
            + SearchHistoryColumns.TIMESTAMP + " INTEGER DEFAULT 0"
            + " )";
    // @formatter:on

    // @formatter:off
    private final static String SQL_CREATE_SEARCH_HISTORY_ADD_TRIGGER =
        "CREATE TRIGGER search_history_add_trigger AFTER INSERT ON " + SEARCH_HISTORY_TABLE + " BEGIN "
            // remove old entries
            + " DELETE FROM " + SEARCH_HISTORY_TABLE + " WHERE " + SearchHistoryColumns._ID + " not in"
                + "("
                    + " select " + SearchHistoryColumns._ID + " from " + SEARCH_HISTORY_TABLE + " order by " + SearchHistoryColumns._ID + " desc limit " + SEARCH_HISTORY_SIZE
                + ");"
            // mark all existing entries as historic
            + " UPDATE " + SEARCH_HISTORY_TABLE + " SET " + SearchHistoryColumns.HISTORIC + "=1 " +  " WHERE "
                + SearchHistoryColumns._ID + " <  new." + SearchHistoryColumns._ID + " AND " + SearchHistoryColumns.HISTORIC + "=0 ;"
            + " END";
    // @formatter:on


    public SearchHistoryDatabaseHelper(Context context)
    {
        super(context, SEARCH_HISTORY_DATABASE, null, VERSION);
    }


    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_SEARCH_HISTORY_TABLE);
        db.execSQL(SQL_CREATE_SEARCH_HISTORY_ADD_TRIGGER);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // nothing to do, yet
    }

}
