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

package org.dmfs.provider.tasks.utils;

import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.adapters.SinglePresent;
import org.dmfs.jems.procedure.Procedure;
import org.dmfs.jems.single.Single;


/**
 * Experiemental Procedure which calls another procedure with a given value.
 * <p>
 * TODO move to jems if this works out well
 *
 * @author Marten Gajda
 */
@Deprecated
public final class With<T> implements Procedure<Procedure<T>>
{
    private final Optional<T> mValue;


    public With(T value)
    {
        this(() -> value);
    }


    public With(Single<T> value)
    {
        this(new SinglePresent<>(value));
    }


    public With(Optional<T> value)
    {
        mValue = value;
    }


    @Override
    public void process(Procedure<T> delegate)
    {
        if (mValue.isPresent())
        {
            delegate.process(mValue.value());
        }
    }
}
