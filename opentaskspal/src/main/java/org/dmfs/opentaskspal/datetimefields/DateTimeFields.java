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

package org.dmfs.opentaskspal.datetimefields;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.Nullable;


/**
 * The three values that together represent a combined date-time in the provider. (Start or due date-time in practice.)
 * Corresponds one-to-one to the actual values in the database,
 * and thus to the values in {@link Cursor}, {@link ContentValues}, and ContentSet.
 *
 * @author Gabor Keszthelyi
 */
public interface DateTimeFields
{

    /**
     * The timestamp. ({@code null} if it's empty)
     */
    @Nullable
    Long timestamp();

    /**
     * Time zone id.  ({@code null} if it's empty)
     */
    @Nullable
    String timeZoneId();

    /**
     * All-day flag of the date-time. 1 for true, 0 for false.
     * <p>
     * ({@code null} if it's empty, which is always interpreted as 0-false)
     */
    @Nullable
    Long isAllDay();

}
