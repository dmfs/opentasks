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

package org.dmfs.provider.tasks.utils.iterator;

import org.dmfs.iterators.AbstractBaseIterator;

import java.util.Iterator;


/**
 * @author Marten Gajda
 * @deprecated move to jems
 */
@Deprecated
public final class Alternating<T> extends AbstractBaseIterator<T>
{
    private final Iterator<T> mFirst;
    private final Iterator<T> mSecond;
    private boolean mNextFirst = false;


    public Alternating(Iterator<T> first, Iterator<T> second)
    {
        mFirst = first;
        mSecond = second;
    }


    @Override
    public boolean hasNext()
    {
        return mFirst.hasNext() && mSecond.hasNext();
    }


    @Override
    public T next()
    {
        mNextFirst = !mNextFirst;
        return mNextFirst ? mFirst.next() : mSecond.next();
    }
}
