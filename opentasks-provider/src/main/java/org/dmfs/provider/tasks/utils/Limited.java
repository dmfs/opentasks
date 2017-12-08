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

import java.util.Iterator;


/**
 * An {@link Iterable} which limits the number of elements.
 * <p>
 * TODO: move to jems
 *
 * @author Marten Gajda
 * @deprecated
 */
@Deprecated
public final class Limited<T> implements Iterable<T>
{
    private final int mCount;
    private final Iterable<T> mDelegate;


    public Limited(int count, Iterable<T> delegate)
    {
        mCount = count;
        mDelegate = delegate;
    }


    @Override
    public Iterator<T> iterator()
    {
        return new LimitedIterator<>(mCount, mDelegate.iterator());
    }
}
