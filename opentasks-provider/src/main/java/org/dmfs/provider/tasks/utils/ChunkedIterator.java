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

import org.dmfs.iterators.AbstractBaseIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


/**
 * In {@link Iterator} decorator which returns the elements of the decorated {@link Iterator} in chunks of a specific size.
 *
 * @author Marten Gajda
 * @deprecated TODO: Move to jems.
 */
public final class ChunkedIterator<T> extends AbstractBaseIterator<Iterable<T>>
{
    private final int mChunkSize;
    private final Iterator<T> mDelegate;


    public ChunkedIterator(int chunkSize, Iterator<T> delegate)
    {
        if (chunkSize <= 0)
        {
            throw new IllegalArgumentException(String.format(Locale.ENGLISH, "Chunk size must be >0 but was %s", chunkSize));
        }
        mChunkSize = chunkSize;
        mDelegate = delegate;
    }


    @Override
    public boolean hasNext()
    {
        return mDelegate.hasNext();
    }


    @Override
    public Iterable<T> next()
    {
        List<T> result = new ArrayList<>(mChunkSize);
        int remaining = mChunkSize;
        do
        {
            result.add(mDelegate.next());
        } while (mDelegate.hasNext() && --remaining > 0);
        return result;
    }
}
