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

package org.dmfs.tasks.model.contentset;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.opentaskspal.datetimefields.DateTimeFields;
import org.dmfs.opentaskspal.datetimefields.DelegatingDateTimeFields;
import org.dmfs.opentaskspal.datetimefields.Evaluated;
import org.dmfs.tasks.model.ContentSet;


/**
 * @author Gabor Keszthelyi
 */
public final class ContentSetDateTimeFields extends DelegatingDateTimeFields
{

    public ContentSetDateTimeFields(@NonNull ContentSet contentSet,
                                    @NonNull String timestampKey,
                                    @NonNull String timeZoneKey,
                                    @NonNull String isAllDayKey)
    {
        super(new Evaluated(new DeferringContentSetDateTimeFields(contentSet, timestampKey, timeZoneKey, isAllDayKey)));
    }


    private static final class DeferringContentSetDateTimeFields implements DateTimeFields
    {
        private final ContentSet mContentSet;
        private final String mTimestampKey;
        private final String mTimeZoneKey;
        private final String mIsAllDayKey;


        public DeferringContentSetDateTimeFields(@NonNull ContentSet contentSet,
                                                 @NonNull String timestampKey,
                                                 @NonNull String timeZoneKey,
                                                 @NonNull String isAllDayKey)
        {
            mContentSet = contentSet;
            mTimestampKey = timestampKey;
            mTimeZoneKey = timeZoneKey;
            mIsAllDayKey = isAllDayKey;
        }


        @Nullable
        @Override
        public Long timestamp()
        {
            return mContentSet.getAsLong(mTimestampKey);
        }


        @Nullable
        @Override
        public String timeZoneId()
        {
            return mContentSet.getAsString(mTimeZoneKey);
        }


        @Nullable
        @Override
        public Long isAllDay()
        {
            return mContentSet.getAsLong(mIsAllDayKey);
        }
    }
}
