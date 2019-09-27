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

package org.dmfs.opentaskspal.tasks;

import android.content.ContentProviderOperation;

import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.TimeZone;

import static org.dmfs.android.contentpal.testing.contentoperationbuilder.WithValues.withValuesOnly;
import static org.dmfs.android.contentpal.testing.contentvalues.Containing.containing;
import static org.dmfs.android.contentpal.testing.contentvalues.NullValue.withNullValue;
import static org.dmfs.android.contentpal.testing.rowdata.RowDataMatcher.builds;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;


/**
 * Unit test for {@link TimeData}.
 *
 * @author Gabor Keszthelyi
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class TimeDataTest
{
    @Test
    public void test_whenStartAndDueAreProvided_setsThemAndNullsDuration()
    {
        DateTime start = DateTime.now();
        DateTime due = start.addDuration(new Duration(1, 1, 0));

        assertThat(new TimeData<>(start, due),
                builds(
                        withValuesOnly(
                                containing(Tasks.DTSTART, start.getTimestamp()),
                                containing(Tasks.TZ, "UTC"),
                                containing(Tasks.IS_ALLDAY, 0),
                                containing(Tasks.DUE, due.getTimestamp()),
                                withNullValue(Tasks.DURATION)
                        )));
    }


    @Test
    public void test_whenStartAndDurationAreProvided_setsThemAndNullsDue()
    {
        DateTime start = DateTime.now();
        Duration duration = new Duration(1, 3, 0);

        assertThat(new TimeData<>(start, duration),
                builds(
                        withValuesOnly(
                                containing(Tasks.DTSTART, start.getTimestamp()),
                                containing(Tasks.TZ, "UTC"),
                                containing(Tasks.IS_ALLDAY, 0),
                                withNullValue(Tasks.DUE),
                                containing(Tasks.DURATION, duration.toString())
                        )));
    }


    @Test
    public void test_whenOnlyStartIsProvided_setsItAndNullsDueAndDuration()
    {
        DateTime start = DateTime.now();

        assertThat(new TimeData<>(start),
                builds(
                        withValuesOnly(
                                containing(Tasks.DTSTART, start.getTimestamp()),
                                containing(Tasks.TZ, "UTC"),
                                containing(Tasks.IS_ALLDAY, 0),
                                withNullValue(Tasks.DUE),
                                withNullValue(Tasks.DURATION)
                        )));
    }


    @Test(expected = IllegalArgumentException.class)
    public void test_whenStartIsAllDayAndDueIsNot_throwsIllegalArgument()
    {
        new TimeData<>(DateTime.now().toAllDay(), DateTime.now())
                .updatedBuilder(mock(TransactionContext.class), mock(ContentProviderOperation.Builder.class));
    }


    @Test(expected = IllegalArgumentException.class)
    public void test_whenDueIsAllDayAndStartIsNot_throwsIllegalArgument()
    {
        new TimeData<>(DateTime.now(), DateTime.now().toAllDay())
                .updatedBuilder(mock(TransactionContext.class), mock(ContentProviderOperation.Builder.class));
    }


    @Test
    public void test_whenStartHasDifferentTimeZoneFromDue_shiftsStartsToDue()
    {
        DateTime start = DateTime.now().swapTimeZone(TimeZone.getTimeZone("GMT+3"));
        DateTime due = start.addDuration(new Duration(1, 3, 0)).swapTimeZone(TimeZone.getTimeZone("GMT+6"));

        DateTime startExpected = start.shiftTimeZone(TimeZone.getTimeZone("GMT+6"));

        assertThat(new TimeData<>(start, due),
                builds(
                        withValuesOnly(
                                containing(Tasks.DTSTART, startExpected.getTimestamp()),
                                containing(Tasks.TZ, "GMT+06:00"),
                                containing(Tasks.IS_ALLDAY, 0),
                                containing(Tasks.DUE, due.getTimestamp()),
                                withNullValue(Tasks.DURATION)
                        )));
    }


    @Test
    public void test_whenStartHasAllDayFlag_correspondingValueIsOne()
    {
        DateTime start = DateTime.now().toAllDay();
        DateTime due = start.addDuration(new Duration(1, 3, 0));

        assertThat(new TimeData<>(start, due),
                builds(
                        withValuesOnly(
                                containing(Tasks.DTSTART, start.getTimestamp()),
                                containing(Tasks.TZ, "UTC"),
                                containing(Tasks.IS_ALLDAY, 1),
                                containing(Tasks.DUE, due.getTimestamp()),
                                withNullValue(Tasks.DURATION)
                        )));
    }
}