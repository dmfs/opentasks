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

package org.dmfs.provider.tasks.utils;

import org.dmfs.iterators.AbstractBaseIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * An {@link Iterator} which limits the number elements.
 * TODO: move to jems
 *
 * @author Marten Gajda
 * @deprecated
 */
@Deprecated
public final class LimitedIterator<T> extends AbstractBaseIterator<T>
{
    private int mCount;
    private final Iterator<T> mDelegate;


    public LimitedIterator(int count, Iterator<T> delegate)
    {
        mCount = count;
        mDelegate = delegate;
    }


    @Override
    public boolean hasNext()
    {
        return mCount > 0 && mDelegate.hasNext();
    }


    @Override
    public T next()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException("No more elements to iterate");
        }
        mCount--;
        return mDelegate.next();
    }
}
