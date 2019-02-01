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

package org.dmfs.tasks.utils;

import org.dmfs.jems.fragile.Fragile;
import org.dmfs.jems.single.Single;


/**
 * 'Unchecks' an Exception, i.e. turns a {@link Fragile} into a {@link Single} by rethrowing
 * the possible {@link Exception} as {@link RuntimeException}.
 * <p>
 * Note: This should be used with care for obvious reasons, only at appropriate places.
 *
 * @author Gabor Keszthelyi
 * @deprecated use it from jems when available
 */
@Deprecated
public final class Unchecked<T> implements Single<T>
{
    private final Fragile<T, Exception> mDelegate;


    public Unchecked(Fragile<T, Exception> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public T value()
    {
        try
        {
            return mDelegate.value();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception in Unchecked", e);
        }
    }
}
