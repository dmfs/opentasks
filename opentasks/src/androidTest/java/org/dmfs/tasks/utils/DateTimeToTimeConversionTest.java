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

package org.dmfs.tasks.utils;

import android.support.test.runner.AndroidJUnit4;
import android.text.format.Time;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.TimeZone;


/**
 * Test for {@link DateFormatter#toTime(DateTime)} method.
 *
 * @author Gabor Keszthelyi
 */
@RunWith(AndroidJUnit4.class)
public class DateTimeToTimeConversionTest
{

    @Test
    public void test_toTime_withVariousDateTimes()
    {
        assertCorrectlyConverted(DateTime.now());

        assertCorrectlyConverted(DateTime.now(TimeZone.getTimeZone("UTC+04:00")));

        assertCorrectlyConverted(DateTime.nowAndHere());

        assertCorrectlyConverted(new DateTime(1509473781000L));

        assertCorrectlyConverted(new DateTime(1509473781000L).addDuration(new Duration(1, 1, 0)));

        assertCorrectlyConverted(DateTime.now(TimeZone.getTimeZone("UTC+04:00")).shiftTimeZone(TimeZone.getTimeZone("UTC+05:00")));

        // Floating, all-day
        assertCorrectlyConverted(DateTime.now().toAllDay());

        // Not DST (March 2017 in Hungary):
        assertCorrectlyConverted(new DateTime(TimeZone.getTimeZone("Europe/Budapest"), 2017, 2 - 1, 7, 15, 0, 0));
        assertCorrectlyConverted(new DateTime(2017, 2 - 1, 7, 15, 0, 0).shiftTimeZone(TimeZone.getTimeZone("Europe/Budapest")));
        assertCorrectlyConverted(new DateTime(2017, 2 - 1, 7, 15, 0, 0).swapTimeZone(TimeZone.getTimeZone("Europe/Budapest")));

        // DST (July 2017 in Hungary):
        assertCorrectlyConverted(new DateTime(TimeZone.getTimeZone("Europe/Budapest"), 2017, 7 - 1, 7, 15, 0, 0));
        assertCorrectlyConverted(new DateTime(2017, 7 - 1, 7, 15, 0, 0).shiftTimeZone(TimeZone.getTimeZone("Europe/Budapest")));
        assertCorrectlyConverted(new DateTime(2017, 7 - 1, 7, 15, 0, 0).swapTimeZone(TimeZone.getTimeZone("Europe/Budapest")));
    }


    @Test(expected = IllegalArgumentException.class)
    public void test_toTime_forFloatingButNotAllDayDateTime_throwsSinceItIsNotSupported()
    {
        new DateFormatter(null).toTime(new DateTime(2017, 7 - 1, 7, 15, 0, 0));
    }


    private void assertCorrectlyConverted(DateTime dateTime)
    {
        Time time = new DateFormatter(null).toTime(dateTime);
        if (!isEquivalentDateTimeAndTime(dateTime, time))
        {
            throw new AssertionError(String.format("DateTime=%s and Time=%s are not equivalent", dateTime, time));
        }
    }


    /**
     * Contains the definition/requirement of when a {@link DateTime} and {@link Time} is considered equivalent in this project.
     */
    private boolean isEquivalentDateTimeAndTime(DateTime dateTime, Time time)
    {
        // android.text.Time doesn't seem to store in millis precision, there is a 1000 multiplier used there internally
        // when calculating millis, so we can only compare in this precision:
        boolean millisMatch =
                dateTime.getTimestamp() / 1000
                        ==
                        time.toMillis(false) / 1000;

        boolean allDaysMatch = time.allDay == dateTime.isAllDay();

        boolean timeZoneMatch =
                // If DateTime is floating, all-day then if the all-day flag is matched with Time (checked earlier)
                // then we consider the Time's timezone matching, we ignore that basically,
                // because Time always has a time zone, and there is no other way to represent all-day date-times with Time.
                (dateTime.isFloating() && dateTime.isAllDay())
                        ||
                        // This is the regular case with non-floating DateTime
                        (dateTime.getTimeZone() != null && time.timezone.equals(dateTime.getTimeZone().getID()));

        return millisMatch && allDaysMatch && timeZoneMatch;
    }

}