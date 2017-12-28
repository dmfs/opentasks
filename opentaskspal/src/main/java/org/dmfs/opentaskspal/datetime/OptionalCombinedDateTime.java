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

import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.single.Single;
import org.dmfs.opentaskspal.datetime.general.OptionalTimeZone;
import org.dmfs.opentaskspal.datetimefields.DateTimeFields;
import org.dmfs.opentaskspal.utils.binarybooleans.BinaryLongBoolean;
import org.dmfs.optional.NullSafe;
import org.dmfs.optional.Optional;
import org.dmfs.optional.decorators.DelegatingOptional;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;

import java.util.TimeZone;


/**
 * {@link Optional} {@link DateTime} value composed from the three related fields' values (timestamp, timezone, all-day)
 * interpreted according to {@link TaskContract}.
 *
 * @author Gabor Keszthelyi
 */
public final class OptionalCombinedDateTime extends DelegatingOptional<DateTime>
{

    public OptionalCombinedDateTime(Optional<Long> timestamp,
                                    Optional<TimeZone> timeZone,
                                    Single<Boolean> isAllDay)
    {
        super(new Mapped<>(
                ts -> new CombinedDateTime(ts, timeZone, isAllDay).value(),
                timestamp)
        );
    }


    public OptionalCombinedDateTime(@NonNull DateTimeFields fields)
    {
        this(new NullSafe<>(fields.timestamp()),
                new OptionalTimeZone(fields.timeZoneId()),
                new BinaryLongBoolean(fields.isAllDay()));
    }

}
