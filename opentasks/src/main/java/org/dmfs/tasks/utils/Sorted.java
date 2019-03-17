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

package org.dmfs.tasks.utils;

import org.dmfs.jems.single.elementary.Reduced;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * An {@link Iterable} decorator which returns the elements of the delegate in a sorted order, determined by a given {@link Comparator}.
 *
 * @author Marten Gajda
 */
public final class Sorted<T> implements Iterable<T>
{
    private final Iterable<T> mDelegate;
    private final Comparator<T> mComparator;


    public Sorted(Comparator<T> comparator, Iterable<T> delegate)
    {
        mDelegate = delegate;
        mComparator = comparator;
    }


    @Override
    public Iterator<T> iterator()
    {
        return new Reduced<>(new TreeSet<>(mComparator), (r, v) -> {
            r.add(v);
            return r;
        }, mDelegate).value().iterator();
    }
}
