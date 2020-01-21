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

package org.dmfs.opentaskstestpal;

import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.contract.TaskContract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.TimeZone;

import static org.dmfs.android.contentpal.testing.contentoperationbuilder.WithValues.withValuesOnly;
import static org.dmfs.android.contentpal.testing.contentvalues.Containing.containing;
import static org.dmfs.android.contentpal.testing.contentvalues.NullValue.withNullValue;
import static org.dmfs.android.contentpal.testing.rowdata.RowDataMatcher.builds;
import static org.dmfs.jems.optional.elementary.Absent.absent;
import static org.junit.Assert.assertThat;


/**
 * @author Marten Gajda
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class InstanceTestDataTest
{
    @Test
    public void testNoDate()
    {
        assertThat(new InstanceTestData(5),
                builds(
                        withValuesOnly(
                                withNullValue(TaskContract.Instances.INSTANCE_START),
                                withNullValue(TaskContract.Instances.INSTANCE_START_SORTING),
                                withNullValue(TaskContract.Instances.INSTANCE_DUE),
                                withNullValue(TaskContract.Instances.INSTANCE_DUE_SORTING),
                                withNullValue(TaskContract.Instances.INSTANCE_DURATION),
                                withNullValue(TaskContract.Instances.INSTANCE_ORIGINAL_TIME),
                                containing(TaskContract.Instances.DISTANCE_FROM_CURRENT, 5),
                                withNullValue(TaskContract.Instances.DTSTART),
                                withNullValue(TaskContract.Instances.DUE),
                                withNullValue(TaskContract.Instances.ORIGINAL_INSTANCE_TIME),
                                withNullValue(TaskContract.Instances.DURATION),
                                withNullValue(TaskContract.Instances.RRULE),
                                withNullValue(TaskContract.Instances.RDATE),
                                withNullValue(TaskContract.Instances.EXDATE)
                        )
                ));
    }


    @Test
    public void testWithDate()
    {
        DateTime start = DateTime.now();
        DateTime due = start.addDuration(Duration.parse("P1DT1H"));
        assertThat(new InstanceTestData(start, due, absent(), 5),
                builds(
                        withValuesOnly(
                                containing(TaskContract.Instances.INSTANCE_START, start.getTimestamp()),
                                containing(TaskContract.Instances.INSTANCE_START_SORTING, start.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                                containing(TaskContract.Instances.INSTANCE_DUE, due.getTimestamp()),
                                containing(TaskContract.Instances.INSTANCE_DUE_SORTING, due.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                                containing(TaskContract.Instances.INSTANCE_DURATION, due.getTimestamp() - start.getTimestamp()),
                                withNullValue(TaskContract.Instances.INSTANCE_ORIGINAL_TIME),
                                containing(TaskContract.Instances.DISTANCE_FROM_CURRENT, 5),
                                containing(TaskContract.Instances.DTSTART, start.getTimestamp()),
                                containing(TaskContract.Instances.DUE, due.getTimestamp()),
                                withNullValue(TaskContract.Instances.ORIGINAL_INSTANCE_TIME),
                                withNullValue(TaskContract.Instances.DURATION),
                                withNullValue(TaskContract.Instances.RRULE),
                                withNullValue(TaskContract.Instances.RDATE),
                                withNullValue(TaskContract.Instances.EXDATE)
                        )
                ));
    }


    @Test
    public void testWithDateAndOriginalTime()
    {
        DateTime start = DateTime.now();
        DateTime due = start.addDuration(Duration.parse("P1DT1H"));
        DateTime original = start.addDuration(Duration.parse("P2DT2H"));
        assertThat(new InstanceTestData(start, due, new Present<>(original), 5),
                builds(
                        withValuesOnly(
                                containing(TaskContract.Instances.INSTANCE_START, start.getTimestamp()),
                                containing(TaskContract.Instances.INSTANCE_START_SORTING, start.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                                containing(TaskContract.Instances.INSTANCE_DUE, due.getTimestamp()),
                                containing(TaskContract.Instances.INSTANCE_DUE_SORTING, due.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                                containing(TaskContract.Instances.INSTANCE_DURATION, due.getTimestamp() - start.getTimestamp()),
                                containing(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, original.getTimestamp()),
                                containing(TaskContract.Instances.DISTANCE_FROM_CURRENT, 5),
                                containing(TaskContract.Instances.DTSTART, start.getTimestamp()),
                                containing(TaskContract.Instances.DUE, due.getTimestamp()),
                                containing(TaskContract.Instances.ORIGINAL_INSTANCE_TIME, original.getTimestamp()),
                                withNullValue(TaskContract.Instances.DURATION),
                                withNullValue(TaskContract.Instances.RRULE),
                                withNullValue(TaskContract.Instances.RDATE),
                                withNullValue(TaskContract.Instances.EXDATE)
                        )
                ));
    }


    @Test
    public void testWithStartDateAndOriginalTime()
    {
        DateTime start = DateTime.now();
        DateTime original = start.addDuration(Duration.parse("P2DT2H"));
        assertThat(new InstanceTestData(new Present<>(start), absent(), new Present<>(original), 5),
                builds(
                        withValuesOnly(
                                containing(TaskContract.Instances.INSTANCE_START, start.getTimestamp()),
                                containing(TaskContract.Instances.INSTANCE_START_SORTING, start.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                                withNullValue(TaskContract.Instances.INSTANCE_DUE),
                                withNullValue(TaskContract.Instances.INSTANCE_DUE_SORTING),
                                withNullValue(TaskContract.Instances.INSTANCE_DURATION),
                                containing(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, original.getTimestamp()),
                                containing(TaskContract.Instances.DISTANCE_FROM_CURRENT, 5),
                                containing(TaskContract.Instances.DTSTART, start.getTimestamp()),
                                containing(TaskContract.Instances.ORIGINAL_INSTANCE_TIME, original.getTimestamp()),
                                withNullValue(TaskContract.Instances.DUE),
                                withNullValue(TaskContract.Instances.DURATION),
                                withNullValue(TaskContract.Instances.RRULE),
                                withNullValue(TaskContract.Instances.RDATE),
                                withNullValue(TaskContract.Instances.EXDATE)
                        )
                ));
    }


    @Test
    public void testWithDueDateAndOriginalTime()
    {
        DateTime due = DateTime.now();
        DateTime original = due.addDuration(Duration.parse("P2DT2H"));
        assertThat(new InstanceTestData(absent(), new Present<>(due), new Present<>(original), 5),
                builds(
                        withValuesOnly(
                                withNullValue(TaskContract.Instances.INSTANCE_START),
                                withNullValue(TaskContract.Instances.INSTANCE_START_SORTING),
                                containing(TaskContract.Instances.INSTANCE_DUE, due.getTimestamp()),
                                containing(TaskContract.Instances.INSTANCE_DUE_SORTING, due.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                                withNullValue(TaskContract.Instances.INSTANCE_DURATION),
                                containing(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, original.getTimestamp()),
                                containing(TaskContract.Instances.DISTANCE_FROM_CURRENT, 5),
                                withNullValue(TaskContract.Instances.DTSTART),
                                containing(TaskContract.Instances.DUE, due.getTimestamp()),
                                containing(TaskContract.Instances.ORIGINAL_INSTANCE_TIME, original.getTimestamp()),
                                withNullValue(TaskContract.Instances.DURATION),
                                withNullValue(TaskContract.Instances.RRULE),
                                withNullValue(TaskContract.Instances.RDATE),
                                withNullValue(TaskContract.Instances.EXDATE)
                        )
                ));
    }
}