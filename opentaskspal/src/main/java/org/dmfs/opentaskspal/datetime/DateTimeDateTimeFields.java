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

package org.dmfs.opentaskspal.datetime;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.opentaskspal.datetimefields.DateTimeFields;
import org.dmfs.rfc5545.DateTime;

import java.util.TimeZone;


/**
 * @author Gabor Keszthelyi
 */
public final class DateTimeDateTimeFields implements DateTimeFields
{
    private final DateTime mDateTime;


    public DateTimeDateTimeFields(@NonNull DateTime dateTime)
    {
        mDateTime = dateTime;
    }


    @Nullable
    @Override
    public Long timestamp()
    {
        return mDateTime.getTimestamp();
    }


    @Nullable
    @Override
    public String timeZoneId()
    {
        if (mDateTime.isAllDay())
        {
            return null;
        }
        TimeZone timeZone = mDateTime.getTimeZone();
        return timeZone == null ? null : timeZone.getID();
    }


    @Nullable
    @Override
    public Long isAllDay()
    {
        return mDateTime.isAllDay() ? 1L : 0L;
    }
}
