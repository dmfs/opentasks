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

package org.dmfs.opentaskspal.datetimefields.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.opentaskspal.datetimefields.DateTimeFields;


/**
 * {@link DateTimeFields} from a {@link RowDataSnapshot}.
 *
 * @author Gabor Keszthelyi
 */
public final class RowDataSnapshotDateTimeFields implements DateTimeFields
{
    private final RowDataSnapshot<?> mRowDataSnapshot;
    private final String mTimestampKey;
    private final String mTimeZoneKey;
    private final String mIsAllDayKey;


    public RowDataSnapshotDateTimeFields(@NonNull RowDataSnapshot<?> rowDataSnapshot,
                                         @NonNull String timestampKey,
                                         @NonNull String timeZoneKey,
                                         @NonNull String isAllDayKey)
    {
        mRowDataSnapshot = rowDataSnapshot;
        mTimestampKey = timestampKey;
        mTimeZoneKey = timeZoneKey;
        mIsAllDayKey = isAllDayKey;
    }


    @Nullable
    @Override
    public Long timestamp()
    {
        return mRowDataSnapshot.data(mTimestampKey, Long::valueOf).value(null);
    }


    @Nullable
    @Override
    public String timeZoneId()
    {
        return mRowDataSnapshot.data(mTimeZoneKey, s -> s).value(null);
    }


    @Nullable
    @Override
    public Long isAllDay()
    {
        return mRowDataSnapshot.data(mIsAllDayKey, Long::valueOf).value(null);
    }
}
