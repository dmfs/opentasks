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
import org.dmfs.rfc5545.DateTime;


/**
 * {@link Optional} for a {@link Long} timestamp of a provided optional {@link DateTime}.
 *
 * @author Gabor Keszthelyi
 */
public final class OptionalDateTimeTimestamp extends DelegatingOptional<Long>
{
    public OptionalDateTimeTimestamp(@NonNull Optional<DateTime> dateTime)
    {
        super(new Mapped<>(DateTime::getTimestamp, dateTime));
    }


    public OptionalDateTimeTimestamp(@Nullable DateTime dateTime)
    {
        this(new NullSafe<>(dateTime));
    }
}
