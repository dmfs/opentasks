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


/**
 * {@link Single} for a {@link DateTime} that is the same as the provided one except that it is moved to the start of the hour.
 * e.g.: 2018 Jan 18 20:44:33 -> 2018 Jan 18 20:00:00
 *
 * @author Gabor Keszthelyi
 */
public final class StartOfHour extends DelegatingSingle<DateTime>
{
    public StartOfHour(DateTime original)
    {
        super(() -> original.isAllDay() ?
                // All-day date-time is already at the start of the hour with 00:00
                original
                :
                new DateTime(original.getTimeZone(), original.getYear(),
                        original.getMonth(), original.getDayOfMonth(), original.getHours(), 0, 0));
    }
}
