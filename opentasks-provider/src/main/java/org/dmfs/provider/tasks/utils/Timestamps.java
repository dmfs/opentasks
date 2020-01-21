/*
 * Copyright 2019 dmfs GmbH
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

package org.dmfs.provider.tasks.utils;

import org.dmfs.jems.single.Single;
import org.dmfs.rfc5545.DateTime;


/**
 * A {@link Single} of an array of timestamp values of a given {@link Iterable} of {@link DateTime}s.
 *
 * @author Marten Gajda
 */
public final class Timestamps implements Single<long[]>
{
    private final Iterable<DateTime> mDateTimes;


    public Timestamps(Iterable<DateTime> dateTimes)
    {
        mDateTimes = dateTimes;
    }


    @Override
    public long[] value()
    {
        int count = 0;
        for (DateTime ignored : mDateTimes)
        {
            count += 1;
        }
        long[] timeStamps = new long[count];
        int i = 0;
        for (DateTime dt : mDateTimes)
        {
            timeStamps[i++] = dt.getTimestamp();
        }
        return timeStamps;
    }
}
