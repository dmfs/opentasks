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

import android.support.annotation.Nullable;


/**
 * The 3 values that together represent a start/due date-time in the provider.
 * Corresponds one to one to the actual values in the database.
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
     * All-day flag of the date-time. 1 for true, 0 for false. ({@code null} if it's empty)
     */
    @Nullable
    Long isAllDay();

}
