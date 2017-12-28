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

package org.dmfs.opentaskspal.datetimefields.decorators;

import android.support.annotation.Nullable;

import org.dmfs.opentaskspal.datetimefields.DateTimeFields;


/**
 * Eager evaluation decorator for {@link DateTimeFields}.
 *
 * @author Gabor Keszthelyi
 */
public final class Evaluated implements DateTimeFields
{
    private final Long mTimestamp;
    private final String mTimeZoneId;
    private final Long mIsAllDay;


    public Evaluated(DateTimeFields delegate)
    {
        mTimestamp = delegate.timestamp();
        mTimeZoneId = delegate.timeZoneId();
        mIsAllDay = delegate.isAllDay();
    }


    @Nullable
    @Override
    public Long timestamp()
    {
        return mTimestamp;
    }


    @Nullable
    @Override
    public String timeZoneId()
    {
        return mTimeZoneId;
    }


    @Nullable
    @Override
    public Long isAllDay()
    {
        return mIsAllDay;
    }
}
