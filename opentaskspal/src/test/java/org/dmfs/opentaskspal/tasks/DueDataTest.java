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

import org.dmfs.rfc5545.DateTime;
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
import static org.junit.Assert.assertThat;


/**
 * Unit test for {@link DueData}.
 *
 * @author Gabor Keszthelyi
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class DueDataTest
{

    @Test
    public void test_whenNoTimeZoneNotAllDay_setsValuesAccordingly_andNullsOtherTimeRelatedValues()
    {
        DateTime due = DateTime.now();

        assertThat(new DueData<>(due),
                builds(
                        withValuesOnly(
                                containing(Tasks.DUE, due.getTimestamp()),
                                containing(Tasks.TZ, "UTC"),
                                containing(Tasks.IS_ALLDAY, 0),
                                withNullValue(Tasks.DTSTART),
                                withNullValue(Tasks.DURATION)
                        )));
    }


    @Test
    public void test_whenHasTimeZoneNotAllDay_setsValuesAccordingly_andNullsOtherTimeRelatedValues()
    {
        DateTime due = DateTime.now().shiftTimeZone(TimeZone.getTimeZone("GMT+4"));

        assertThat(new DueData<>(due),
                builds(
                        withValuesOnly(
                                containing(Tasks.DUE, due.getTimestamp()),
                                containing(Tasks.TZ, "GMT+04:00"),
                                containing(Tasks.IS_ALLDAY, 0),
                                withNullValue(Tasks.DTSTART),
                                withNullValue(Tasks.DURATION)
                        )));
    }


    @Test
    public void test_whenNoTimeZoneAndAllDay_setsValuesAccordingly_andNullsOtherTimeRelatedValues()
    {
        DateTime due = DateTime.now().toAllDay();

        assertThat(new DueData<>(due),
                builds(
                        withValuesOnly(
                                containing(Tasks.DUE, due.getTimestamp()),
                                containing(Tasks.TZ, "UTC"),
                                containing(Tasks.IS_ALLDAY, 1),
                                withNullValue(Tasks.DTSTART),
                                withNullValue(Tasks.DURATION)
                        )));
    }

}