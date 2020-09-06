/*
 * Copyright 2020 dmfs GmbH
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

import org.dmfs.iterators.AbstractBaseIterator;

import java.util.NoSuchElementException;


/**
 * @author Marten Gajda
 */
public final class RowIterator extends AbstractBaseIterator<Cursor>
{
    private final Cursor mCursor;


    public RowIterator(Cursor cursor)
    {
        mCursor = cursor;
    }


    @Override
    public boolean hasNext()
    {
        return mCursor.getCount() > 0 && !mCursor.isClosed() && !mCursor.isLast();
    }


    @Override
    public Cursor next()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException("No other rows to iterate.");
        }
        mCursor.moveToNext();
        return mCursor;
    }
}
