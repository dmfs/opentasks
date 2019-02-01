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

import org.dmfs.jems.function.BiFunction;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.jems.single.decorators.DelegatingSingle;


/**
 * Experimental {@link Single} which applies a {@link BiFunction} based on the presence of an {@link Optional}.
 * <p>
 * TODO: maybe a more appropriate name?
 * <p>
 * TODO: move to jems
 *
 * @author Marten Gajda
 */
@Deprecated
public final class Zipped<T> extends DelegatingSingle<T>
{
    public <V> Zipped(Optional<V> optionalValue, Single<T> delegate, BiFunction<V, T, T> function)
    {
        super(new Backed<T>(new Mapped<>(from -> function.value(from, delegate.value()), optionalValue), delegate));
    }
}
