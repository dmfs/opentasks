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

import org.dmfs.jems.iterator.generators.IntSequenceGenerator;

import java.util.Iterator;


/**
 * An {@link Iterable} which iterates a range of numbers.
 * <p>
 * TODO: implement in jems
 *
 * @author Marten Gajda
 */
@Deprecated
public final class Range implements Iterable<Integer>
{
    private final int mStart;
    private final int mEnd;


    public Range(int end)
    {
        this(0, end);
    }


    public Range(int start, int end)
    {
        mStart = start;
        mEnd = end;
    }


    @Override
    public Iterator<Integer> iterator()
    {
        return new LimitedIterator<>(mEnd - mStart, new IntSequenceGenerator(mStart));
    }
}
