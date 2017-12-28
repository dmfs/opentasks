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

package org.dmfs.opentaskspal.utils.binarybooleans;

import android.support.annotation.Nullable;

import org.dmfs.jems.single.Single;


/**
 * Adapter from binary {@link Long} values 0 and 1 to {@link Boolean}.
 * {@code null} is considered as {@code false}.
 * <p>
 * (Also any other invalid value is considered as {@code false} as well.)
 *
 * @author Gabor Keszthelyi
 */
public final class BinaryLongBoolean implements Single<Boolean>
{
    private static final Long ONE = 1L;

    private final Long mLongValue;


    public BinaryLongBoolean(@Nullable Long longValue)
    {
        mLongValue = longValue;
    }


    @Override
    public Boolean value()
    {
        return ONE.equals(mLongValue);
    }
}
