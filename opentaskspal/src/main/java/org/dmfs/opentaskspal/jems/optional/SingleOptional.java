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

import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.elementary.Frozen;
import org.dmfs.optional.Optional;

import java.util.NoSuchElementException;


/**
 * {@link Optional} that takes a {@link Single} of {@link Optional} and caches its value upon usage.
 *
 * @author Gabor Keszthelyi
 * @deprecated use it from jems when available
 */
@Deprecated
public final class SingleOptional<T> implements Optional<T>
{
    private Single<Optional<T>> mSingleOptional;


    public SingleOptional(Single<Optional<T>> singleOptional)
    {
        mSingleOptional = new Frozen<>(singleOptional);
    }


    @Override
    public boolean isPresent()
    {
        return mSingleOptional.value().isPresent();
    }


    @Override
    public T value(T defaultValue)
    {
        return mSingleOptional.value().value(defaultValue);
    }


    @Override
    public T value() throws NoSuchElementException
    {
        return mSingleOptional.value().value();
    }
}
