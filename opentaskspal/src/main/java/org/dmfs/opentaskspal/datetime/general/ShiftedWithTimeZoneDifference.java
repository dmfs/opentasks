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

import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.decorators.DelegatingSingle;
import org.dmfs.rfc5545.DateTime;

import java.util.TimeZone;


/**
 * {@link Single} for {@link DateTime} that has it's absolute time shifted with the difference
 * between its time zone and the provided time zone.
 * <p>
 * Example:
 * <p>
 * <pre>
 * input time: 2013-04-02 16:00 Europe/Berlin (GMT+02:00)
 * input timeZone: America/New_York (GMT-04:00)
 * </pre>
 * <p>
 * will result in
 * <p>
 * <pre>
 * 2013-04-02 10:00 Europe/Berlin (because the original time is equivalent to 2013-04-02 10:00 America/New_York)
 * </pre>
 * <p>
 * All-day times are not modified.
 *
 * @author Gabor Keszthelyi
 */
// TODO Is this what we need?
// TODO Test
public final class ShiftedWithTimeZoneDifference extends DelegatingSingle<DateTime>
{
    public ShiftedWithTimeZoneDifference(TimeZone timeZone, DateTime original)
    {
        super(() ->
        {
            if (original.isAllDay())
            {
                return original;
            }

            TimeZone originalTimeZone = original.getTimeZone();

            return original.shiftTimeZone(timeZone).swapTimeZone(originalTimeZone);
        });
    }

}
