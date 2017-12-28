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

import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.decorators.DelegatingSingle;
import org.dmfs.jems.single.elementary.ValueSingle;
import org.dmfs.opentaskspal.datetime.general.OptionalTimeZone;
import org.dmfs.optional.Optional;
import org.dmfs.optional.Present;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;

import java.util.TimeZone;


/**
 * {@link DateTime} value for a task composed from the 3 related fields' values (timestamp, timezone, all-day)
 * interpreted according to {@link TaskContract}.
 *
 * @author Gabor Keszthelyi
 */
public final class CombinedDateTime extends DelegatingSingle<DateTime>
{

    public CombinedDateTime(long timestamp, Optional<TimeZone> timeZone, Single<Boolean> isAllDay)
    {
        super(() -> new OptionalCombinedDateTime(new Present<>(timestamp), timeZone, isAllDay).value());
    }


    public CombinedDateTime(long timestamp, @Nullable String timeZoneId, boolean isAllDay)
    {
        this(timestamp, new OptionalTimeZone(timeZoneId), new ValueSingle<>(isAllDay));
    }


    public CombinedDateTime(long timestamp, @NonNull TimeZone timeZone, boolean isAllDay)
    {
        this(timestamp, new Present<>(timeZone), new ValueSingle<>(isAllDay));
    }

}
