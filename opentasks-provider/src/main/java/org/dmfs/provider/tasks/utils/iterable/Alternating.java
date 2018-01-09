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

package org.dmfs.provider.tasks.utils.iterable;

import java.util.Iterator;


/**
 * @author Marten Gajda
 * @deprecated move to jems
 */
@Deprecated
public final class Alternating<T> implements Iterable<T>
{
    private final Iterable<T> mFirst;
    private final Iterable<T> mSecond;


    public Alternating(Iterable<T> first, Iterable<T> second)
    {
        mFirst = first;
        mSecond = second;
    }


    @Override
    public Iterator<T> iterator()
    {
        return new org.dmfs.provider.tasks.utils.iterator.Alternating<>(mFirst.iterator(), mSecond.iterator());
    }
}
