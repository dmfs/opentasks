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

import org.dmfs.iterators.Function;

import java.util.TimeZone;


/**
 * A {@link Function} to get the {@link TimeZone} of a time zone ID.
 *
 * @author Marten Gajda
 * @deprecated use it from contentpal when available or use solution from ContentPal/issues/136
 */
@Deprecated
public class TimeZoneFunction implements Function<CharSequence, TimeZone>
{
    @Override
    public TimeZone apply(CharSequence timeZoneId)
    {
        return TimeZone.getTimeZone(timeZoneId.toString());
    }
}
