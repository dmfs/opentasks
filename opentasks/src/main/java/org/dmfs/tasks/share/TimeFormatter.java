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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.format.Time;

import org.dmfs.iterators.Function;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.model.adapters.TimeZoneWrapper;
import org.dmfs.tasks.utils.DateFormatter;

import java.util.TimeZone;


/**
 * Function to convert a {@link Time} to a {@link String} also adding timezone.
 *
 * @author Gabor Keszthelyi
 */
public final class TimeFormatter implements Function<Time, String>
{

    private final DateFormatter mDateFormatter;
    private final DateFormatter.DateFormatContext mDateFormatContext;
    private final TimeZone mTimeZone;


    public TimeFormatter(@NonNull DateFormatter dateFormatter,
                         @NonNull DateFormatter.DateFormatContext dateFormatContext,
                         @Nullable TimeZone timeZone)
    {
        mDateFormatter = dateFormatter;
        mDateFormatContext = dateFormatContext;
        mTimeZone = timeZone;
    }


    public TimeFormatter(Context context, ContentSet contentSet)
    {
        this(new DateFormatter(context), DateFormatter.DateFormatContext.DETAILS_VIEW, TaskFieldAdapters.TIMEZONE.get(contentSet));
    }


    @Override
    public String apply(Time time)
    {
        String dateTimeText = mDateFormatter.format(time, mDateFormatContext);
        if (mTimeZone == null)
        {
            return dateTimeText;
        }
        else
        {
            TimeZoneWrapper tzw = new TimeZoneWrapper(mTimeZone);
            String timeZoneText = tzw.getDisplayName(tzw.inDaylightTime(time.toMillis(false)), TimeZone.SHORT);
            return dateTimeText + " " + timeZoneText;
        }
    }
}
