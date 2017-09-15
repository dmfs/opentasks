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

package org.dmfs.opentaskspal.utils;

import org.dmfs.rfc5545.DateTime;

import java.util.TimeZone;


/**
 * {@link Equalator} for comparing two {@link DateTime}-s time zone based on their id ({@link TimeZone#getID()}.
 * If none of them have timezone, they are considered equal.
 *
 * @author Gabor Keszthelyi
 */
public final class TimeZoneEqualator implements Equalator<DateTime>
{
    public static final Equalator<DateTime> INSTANCE = new TimeZoneEqualator();


    @Override
    public boolean areEqual(DateTime d1, DateTime d2)
    {
        TimeZone tz1 = d1.getTimeZone();
        TimeZone tz2 = d2.getTimeZone();
        if (tz1 == null && tz2 == null)
        {
            return true;
        }
        if (tz1 == null || tz2 == null)
        {
            return false;
        }
        return tz1.getID().equals(tz2.getID());
    }
}
