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
import android.support.annotation.Nullable;

import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.optional.NullSafe;
import org.dmfs.optional.Optional;
import org.dmfs.optional.decorators.DelegatingOptional;

import java.util.TimeZone;


/**
 * @author Gabor Keszthelyi
 */
public final class OptionalTimeZone extends DelegatingOptional<TimeZone>
{
    public OptionalTimeZone(@NonNull Optional<String> timeZoneId)
    {
        super(new Mapped<>(TimeZone::getTimeZone, timeZoneId));
    }


    public OptionalTimeZone(@Nullable String timeZoneId)
    {
        this(new NullSafe<>(timeZoneId));
    }
}
