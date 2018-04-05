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
import org.dmfs.rfc5545.Duration;


/**
 * {@link Single} for a {@link DateTime} that represents the start (00:00) of the day after the provided one.
 *
 * @author Gabor Keszthelyi
 */
public final class StartOfNextDay extends DelegatingSingle<DateTime>
{
    public StartOfNextDay(DateTime original)
    {
        // TODO Is there a concern about DST here?
        super(() -> DateTime.now().addDuration(new Duration(1, 1, 0)).startOfDay());
    }


    public StartOfNextDay()
    {
        this(DateTime.now());
    }
}
