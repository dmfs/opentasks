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
import android.support.annotation.NonNull;

import org.dmfs.jems.function.BiFunction;
import org.dmfs.optional.Optional;

import java.util.NoSuchElementException;


/**
 * @author Gabor Keszthelyi
 */
// TODO Should these Cursor adapters go to different module?
public final class NullSafeCursorColumnValue<T> implements Optional<T>
{
    private final Cursor mCursor;
    private final String mColumnName;
    private final BiFunction<Cursor, Integer, T> mGetFunction;

    private Integer mCachedColumnIndex;


    public NullSafeCursorColumnValue(@NonNull Cursor cursor,
                                     @NonNull String columnName,
                                     @NonNull BiFunction<Cursor, Integer, T> getFunction)
    {
        mCursor = cursor;
        mColumnName = columnName;
        mGetFunction = getFunction;
    }


    @Override
    public boolean isPresent()
    {
        return !mCursor.isNull(cachedColumnIndex());
    }


    @Override
    public T value(T defaultValue)
    {
        return isPresent() ? value() : defaultValue;
    }


    @Override
    public T value() throws NoSuchElementException
    {
        return mGetFunction.value(mCursor, cachedColumnIndex());
    }


    private Integer cachedColumnIndex()
    {
        if (mCachedColumnIndex == null)
        {
            mCachedColumnIndex = mCursor.getColumnIndexOrThrow(mColumnName);
        }
        return mCachedColumnIndex;
    }
}
