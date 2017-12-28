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

package org.dmfs.tasks.share;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.iterators.Function;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.model.adapters.TimeZoneWrapper;
import org.dmfs.tasks.utils.DateFormatter;

import java.util.TimeZone;


/**
 * Function to convert a {@link DateTime} to a {@link String} also adding timezone.
 *
 * @author Gabor Keszthelyi
 */
public final class DateTimeFormatterFunction implements Function<DateTime, String>
{

    private final DateFormatter mDateFormatter;
    private final DateFormatter.DateFormatContext mDateFormatContext;
    private final TimeZone mTimeZone;


    public DateTimeFormatterFunction(@NonNull DateFormatter dateFormatter,
                                     @NonNull DateFormatter.DateFormatContext dateFormatContext,
                                     @Nullable TimeZone timeZone)
    {
        mDateFormatter = dateFormatter;
        mDateFormatContext = dateFormatContext;
        mTimeZone = timeZone;
    }


    public DateTimeFormatterFunction(Context context, ContentSet contentSet)
    {
        this(new DateFormatter(context), DateFormatter.DateFormatContext.DETAILS_VIEW, TaskFieldAdapters.TIMEZONE.get(contentSet));
    }


    @Override
    public String apply(DateTime dateTime)
    {
        String dateTimeText = mDateFormatter.format(dateTime, mDateFormatContext);
        if (mTimeZone == null)
        {
            return dateTimeText;
        }
        else
        {
            TimeZoneWrapper tzw = new TimeZoneWrapper(mTimeZone);
            String timeZoneText = tzw.getDisplayName(tzw.inDaylightTime(dateTime.getTimestamp()), TimeZone.SHORT);
            return dateTimeText + " " + timeZoneText;
        }
    }
}
