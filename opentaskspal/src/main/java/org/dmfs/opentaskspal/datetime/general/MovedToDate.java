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

import org.dmfs.jems.single.decorators.DelegatingSingle;
import org.dmfs.rfc5545.DateTime;


/**
 * Moves the date to the given one, keeping the timezone and time of the day.
 *
 * @author Gabor Keszthelyi
 */
public final class MovedToDate extends DelegatingSingle<DateTime>
{
    public MovedToDate(int year, int month, int dayOfMonth, DateTime original)
    {
        super(() -> new DateTime(
                original.getTimeZone(),
                year, month, dayOfMonth,
                original.getHours(), original.getMinutes(), original.getSeconds()
        ));
    }
}
