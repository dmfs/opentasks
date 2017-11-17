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

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.iterators.Function;
import org.dmfs.jems.single.Single;


/**
 * @author Gabor Keszthelyi
 * @deprecated use it from contentpal when available or use solution from ContentPal/issues/136
 */
@Deprecated
final class RowCharData<T, R> implements Single<R>
{
    private final RowDataSnapshot<T> mRowDataSnapshot;
    private final String mColumn;
    private final Function<CharSequence, R> mMapFunction;


    public RowCharData(@NonNull RowDataSnapshot<T> rowDataSnapshot,
                       @NonNull String column,
                       @NonNull Function<CharSequence, R> mapFunction)
    {
        mRowDataSnapshot = rowDataSnapshot;
        mColumn = column;
        mMapFunction = mapFunction;
    }


    @Override
    public R value()
    {
        return mMapFunction.apply(mRowDataSnapshot.charData(mColumn).value());
    }
}
