/*
 * Copyright 2019 dmfs GmbH
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

package org.dmfs.provider.tasks.utils;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.jems.function.Function;

import java.util.LinkedList;
import java.util.List;


/**
 * A {@link Function} which returns all column names of a specific table on a given database.
 *
 * @author Marten Gajda
 */
public final class TableColumns implements Function<SQLiteDatabase, Iterable<String>>
{
    private final String mTableName;


    public TableColumns(String tableName)
    {
        mTableName = tableName;
    }


    @Override
    public Iterable<String> value(SQLiteDatabase db)
    {
        try (Cursor cursor = db.rawQuery(String.format("PRAGMA table_info(%s)", DatabaseUtils.sqlEscapeString(mTableName)), null))
        {
            int nameIdx = cursor.getColumnIndexOrThrow("name");

            List<String> result = new LinkedList<>();
            while (cursor.moveToNext())
            {
                result.add(cursor.getString(nameIdx));
            }

            return result;
        }
    }
}
