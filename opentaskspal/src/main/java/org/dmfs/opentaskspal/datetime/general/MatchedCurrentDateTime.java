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

package org.dmfs.opentaskspal.datetime.general;

import android.support.annotation.NonNull;

import org.dmfs.jems.single.Single;
import org.dmfs.rfc5545.DateTime;

import java.util.TimeZone;


/**
 * @author Gabor Keszthelyi
 */
public final class MatchedCurrentDateTime implements Single<DateTime>
{
    private final TimeZone mTimeZone;
    private final boolean mIsAllDay;


    public MatchedCurrentDateTime(@NonNull TimeZone timeZone, @NonNull boolean isAllDay)
    {
        mTimeZone = timeZone;
        mIsAllDay = isAllDay;
    }


    @Override
    public DateTime value()
    {
        DateTime dateTime = new DateTime(mTimeZone, System.currentTimeMillis());
        return mIsAllDay ? dateTime.toAllDay() : dateTime;
    }
}
