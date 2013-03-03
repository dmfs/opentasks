/*
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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
 * 
 */

package org.dmfs.tasks.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

public class ContentValueMapper {
    private final List<String> StringColumns = new ArrayList<String>();
    private final List<String> IntegerColumns = new ArrayList<String>();
    private final List<String> LongColumns = new ArrayList<String>();

    public ContentValueMapper addString(String... columnName) {
	for (String column : columnName) {
	    StringColumns.add(column);
	}
	return this;
    }

    public ContentValueMapper addInteger(String... columnName) {
	for (String column : columnName) {
	    IntegerColumns.add(column);
	}
	return this;
    }

    public ContentValueMapper addLong(String... columnName) {
	for (String column : columnName) {
	    LongColumns.add(column);
	}
	return this;
    }

    public String[] getColumns() {
	String[] columns = new String[StringColumns.size() + IntegerColumns.size() + LongColumns.size()];

	int i = 0;
	for (String column : StringColumns) {
	    columns[i] = column;
	    ++i;
	}
	for (String column : IntegerColumns) {
	    columns[i] = column;
	    ++i;
	}
	for (String column : LongColumns) {
	    columns[i] = column;
	    ++i;
	}

	return columns;
    }

    public ContentValues map(Cursor cursor) {
	if (cursor == null) {
	    return null;
	}

	ContentValues values = new ContentValues();

	for (String column : StringColumns) {
	    final int index = cursor.getColumnIndexOrThrow(column);
	    if (!cursor.isNull(index)) {
		values.put(column, cursor.getString(index));
	    }
	}

	for (String column : IntegerColumns) {
	    final int index = cursor.getColumnIndexOrThrow(column);
	    if (!cursor.isNull(index)) {
		values.put(column, cursor.getInt(index));
	    }
	}

	for (String column : LongColumns) {
	    final int index = cursor.getColumnIndexOrThrow(column);
	    if (!cursor.isNull(index)) {
		values.put(column, cursor.getLong(index));
	    }
	}

	return values;
    }
}
