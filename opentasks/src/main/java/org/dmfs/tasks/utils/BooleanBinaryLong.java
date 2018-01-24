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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.single.decorators.DelegatingSingle;
import org.dmfs.optional.NullSafe;
import org.dmfs.optional.Optional;


/**
 * Adapter for a {@link Boolean} to a 0, 1 binary representation in {@link Long}.
 * <p>
 * If the {@link Boolean} is absent/null the value is 0.
 *
 * @author Gabor Keszthelyi
 */
public final class BooleanBinaryLong extends DelegatingSingle<Long>
{
    public BooleanBinaryLong(@NonNull Optional<Boolean> booleanOptional)
    {
        super(() -> new Mapped<>(b -> b ? 1L : 0L, booleanOptional).value(0L));
    }


    public BooleanBinaryLong(@Nullable Boolean booleanValue)
    {
        this(new NullSafe<>(booleanValue));
    }
}
