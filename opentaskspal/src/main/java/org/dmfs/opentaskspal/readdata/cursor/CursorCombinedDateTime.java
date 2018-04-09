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

package org.dmfs.opentaskspal.readdata.cursor;

import android.database.Cursor;
import android.support.annotation.NonNull;

import org.dmfs.opentaskspal.datetime.OptionalCombinedDateTime;
import org.dmfs.opentaskspal.datetimefields.adapters.CursorDateTimeFields;
import org.dmfs.optional.decorators.DelegatingOptional;
import org.dmfs.rfc5545.DateTime;


/**
 * Combined date-time from a {@link Cursor}.
 *
 * @author Gabor Keszthelyi
 */
public final class CursorCombinedDateTime extends DelegatingOptional<DateTime>
{
    public CursorCombinedDateTime(@NonNull Cursor cursor,
                                  @NonNull String timestampColumn,
                                  @NonNull String timeZoneColumn,
                                  @NonNull String isAllDayColumn)
    {
        super(new OptionalCombinedDateTime(
                new CursorDateTimeFields(cursor, timestampColumn, timeZoneColumn, isAllDayColumn)));
    }
}
