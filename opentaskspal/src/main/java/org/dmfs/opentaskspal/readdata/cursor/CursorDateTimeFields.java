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

package org.dmfs.opentaskspal.readdata.cursor;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.opentaskspal.datetimefields.DateTimeFields;
import org.dmfs.opentaskspal.datetimefields.DelegatingDateTimeFields;
import org.dmfs.opentaskspal.datetimefields.Evaluated;


/**
 * @author Gabor Keszthelyi
 */
public final class CursorDateTimeFields extends DelegatingDateTimeFields
{
    public CursorDateTimeFields(@NonNull Cursor cursor,
                                @NonNull String timestampColumn,
                                @NonNull String timeZoneColumn,
                                @NonNull String isAllDayColumn)
    {
        super(new Evaluated(new DeferringCursorDateTimeFields(cursor, timestampColumn, timeZoneColumn, isAllDayColumn)));
    }


    private static final class DeferringCursorDateTimeFields implements DateTimeFields
    {
        private final Cursor mCursor;
        private final String mTimestampColumn;
        private final String mTimeZoneColumn;
        private final String mIsAllDayColumn;


        private DeferringCursorDateTimeFields(@NonNull Cursor cursor,
                                              @NonNull String timestampColumn,
                                              @NonNull String timeZoneColumn,
                                              @NonNull String isAllDayColumn)
        {
            mCursor = cursor;
            mTimestampColumn = timestampColumn;
            mTimeZoneColumn = timeZoneColumn;
            mIsAllDayColumn = isAllDayColumn;
        }


        @Nullable
        @Override
        public Long timestamp()
        {
            return new LongCursorColumnValue(mCursor, mTimestampColumn).value(null);
        }


        @Nullable
        @Override
        public String timeZoneId()
        {
            return new StringCursorColumnValue(mCursor, mTimeZoneColumn).value(null);
        }


        @Nullable
        @Override
        public Long isAllDay()
        {
            return new LongCursorColumnValue(mCursor, mIsAllDayColumn).value(null);
        }
    }
}
