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

package org.dmfs.provider.tasks.processors.tasks.instancedata;

import android.content.ContentValues;

import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.decorators.DelegatingSingle;
import org.dmfs.provider.tasks.utils.Zipped;
import org.dmfs.rfc5545.DateTime;

import java.util.TimeZone;


/**
 * A {@link Single} of date and time {@link ContentValues} of an instance.
 *
 * @author Marten Gajda
 */
public final class Dated extends DelegatingSingle<ContentValues>
{

    public Dated(Optional<DateTime> dateTime, String timeStampColumn, String sortingColumn, Single<ContentValues> delegate)
    {
        super(new Zipped<>(
                dateTime,
                delegate,
                (dateTime1, values) ->
                {
                    // add timestamp and sorting
                    values.put(timeStampColumn, dateTime1.getTimestamp());
                    values.put(sortingColumn, dateTime1.isAllDay() ? dateTime1.getInstance() : dateTime1.shiftTimeZone(TimeZone.getDefault()).getInstance());
                    return values;
                }));
    }
}
