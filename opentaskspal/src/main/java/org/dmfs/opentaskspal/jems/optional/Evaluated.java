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

package org.dmfs.opentaskspal.jems.optional;

import org.dmfs.optional.Optional;

import java.util.NoSuchElementException;


/**
 * Eagerly evaluation {@link Optional} decorator.
 *
 * @author Gabor Keszthelyi
 * @deprecated use it from jems when available
 */
@Deprecated
public final class Evaluated<T> implements Optional<T>
{
    private final boolean mIsPresent;
    private final T mValue;


    public Evaluated(Optional<T> delegate)
    {
        mIsPresent = delegate.isPresent();
        mValue = mIsPresent ? delegate.value() : null;
    }


    @Override
    public boolean isPresent()
    {
        return mIsPresent;
    }


    @Override
    public T value(T defaultValue)
    {
        return mIsPresent ? mValue : defaultValue;
    }


    @Override
    public T value() throws NoSuchElementException
    {
        if (!mIsPresent)
        {
            throw new NoSuchElementException("Value absent");
        }
        return mValue;
    }
}
