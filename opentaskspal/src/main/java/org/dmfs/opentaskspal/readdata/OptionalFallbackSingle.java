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

package org.dmfs.opentaskspal.readdata;

import android.support.annotation.NonNull;

import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.elementary.ValueSingle;
import org.dmfs.optional.Optional;


/**
 * @author Gabor Keszthelyi
 * @deprecated use it from jems when available
 */
@Deprecated
final class OptionalFallbackSingle<T> implements Single<T>
{
    private final Optional<T> mOptional;
    private final Single<T> mFallback;


    public OptionalFallbackSingle(@NonNull Optional<T> optional, @NonNull Single<T> fallback)
    {
        mOptional = optional;
        mFallback = fallback;
    }


    public OptionalFallbackSingle(@NonNull Optional<T> optional, @NonNull T fallback)
    {
        this(optional, new ValueSingle<T>(fallback));
    }


    @Override
    public T value()
    {
        return mOptional.isPresent() ? mOptional.value() : mFallback.value();
    }
}
