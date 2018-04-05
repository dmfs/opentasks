/*
 * Copyright 2018 dmfs GmbH
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

package org.dmfs.opentaskspal.readdata.cursor;

import android.database.Cursor;

import org.dmfs.jems.single.Single;


/**
 * @author Gabor Keszthelyi
 */
public final class BooleanCursorColumnValue implements Single<Boolean>
{
    private final Cursor mCursor;
    private final String mColumnName;


    public BooleanCursorColumnValue(Cursor cursor, String columnName)
    {
        mCursor = cursor;
        mColumnName = columnName;
    }


    @Override
    public Boolean value()
    {
        return mCursor.getInt(mCursor.getColumnIndexOrThrow(mColumnName)) == 1;
    }
}
