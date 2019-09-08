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

import android.content.ContentValues;
import android.database.Cursor;

import org.dmfs.jems.predicate.Predicate;

import java.util.Arrays;


/**
 * A {@link Predicate} which determines whether all values of a ContentValues object are present in a {@link Cursor}.
 *
 * @author Marten Gajda
 */
public final class ContainsValues implements Predicate<Cursor>
{
    private final ContentValues mValues;


    public ContainsValues(ContentValues values)
    {
        mValues = values;
    }


    @Override
    public boolean satisfiedBy(Cursor testedInstance)
    {
        for (String key : mValues.keySet())
        {
            int columnIdx = testedInstance.getColumnIndex(key);
            if (columnIdx < 0)
            {
                return false;
            }

            if (testedInstance.getType(columnIdx) == Cursor.FIELD_TYPE_BLOB)
            {
                if (!Arrays.equals(mValues.getAsByteArray(key), testedInstance.getBlob(columnIdx)))
                {
                    return false;
                }
            }
            else
            {
                String stringValue = mValues.getAsString(key);
                if (stringValue != null && !stringValue.equals(testedInstance.getString(columnIdx)) || stringValue == null && !testedInstance.isNull(columnIdx))
                {
                    return false;
                }
            }
        }
        return true;
    }
}
