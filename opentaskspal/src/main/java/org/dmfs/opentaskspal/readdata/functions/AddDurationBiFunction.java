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

package org.dmfs.opentaskspal.readdata.functions;

import org.dmfs.jems.function.BiFunction;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;


/**
 * A {@link BiFunction} which adds {@link DateTime} and {@link Duration} values.
 *
 * @author Marten Gajda
 * @deprecated use it from contentpal when available or use solution from ContentPal/issues/136
 */
@Deprecated
public final class AddDurationBiFunction implements BiFunction<DateTime, Duration, DateTime>
{
    @Override
    public DateTime value(DateTime dateTime, Duration duration)
    {
        return dateTime.addDuration(duration);
    }
}
