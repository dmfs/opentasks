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

package org.dmfs.opentaskspal.readdata;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.jems.hamcrest.matchers.optional.AbsentMatcher;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.junit.Test;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.dmfs.jems.mockito.doubles.TestDoubles.failingMock;
import static org.dmfs.jems.optional.elementary.Absent.absent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;


/**
 * Unit test for {@link EffectiveDueDate}.
 *
 * @author Gabor Keszthelyi
 */
public final class EffectiveDueDateTest
{

    @Test
    public void test_whenDueIsPresent_shouldUseThat()
    {
        long timestamp = System.currentTimeMillis();

        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(new Present<>(timestamp)).when(mockData).data(eq(Tasks.DUE), any());
        doReturn(absent()).when(mockData).data(eq(Tasks.DTSTART), any());
        doReturn(absent()).when(mockData).data(eq(Tasks.DURATION), any());
        doReturn(new Present<>(false)).when(mockData).data(eq(Tasks.IS_ALLDAY), any());
        doReturn(new Present<>(TimeZone.getTimeZone("UTC"))).when(mockData).data(eq(Tasks.TZ), any());

        DateTime actual = new EffectiveDueDate(mockData).value();
        assertEquals(timestamp, actual.getTimestamp());
    }


    @Test
    public void test_whenDueIsAbsent_startIsAbsent_durationIsAbsent_shouldBeAbsent()
    {
        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(absent()).when(mockData).data(eq(Tasks.DUE), any());
        doReturn(absent()).when(mockData).data(eq(Tasks.DTSTART), any());
        doReturn(absent()).when(mockData).data(eq(Tasks.DURATION), any());
        doReturn(new Present<>(false)).when(mockData).data(eq(Tasks.IS_ALLDAY), any());
        doReturn(new Present<>(TimeZone.getTimeZone("UTC"))).when(mockData).data(eq(Tasks.TZ), any());

        assertThat(new EffectiveDueDate(mockData), is(AbsentMatcher.absent()));
    }


    @Test
    public void test_whenDueIsAbsent_startIsPresent_durationIsAbsent_shouldBeAbsent()
    {
        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(absent()).when(mockData).data(eq(Tasks.DUE), any());
        doReturn(new Present<>(234234)).when(mockData).data(eq(Tasks.DTSTART), any());
        doReturn(absent()).when(mockData).data(eq(Tasks.DURATION), any());
        doReturn(new Present<>(false)).when(mockData).data(eq(Tasks.IS_ALLDAY), any());
        doReturn(new Present<>(TimeZone.getTimeZone("UTC"))).when(mockData).data(eq(Tasks.TZ), any());

        assertThat(new EffectiveDueDate(mockData), is(AbsentMatcher.absent()));
    }


    @Test
    public void test_whenDueIsAbsent_startIsAbsent_durationIsPresent_shouldBeAbsent()
    {
        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(absent()).when(mockData).data(eq(Tasks.DUE), any());
        doReturn(absent()).when(mockData).data(eq(Tasks.DTSTART), any());
        doReturn(new Present<>(Duration.parse("P7W"))).when(mockData).data(eq(Tasks.DURATION), any());
        doReturn(new Present<>(false)).when(mockData).data(eq(Tasks.IS_ALLDAY), any());
        doReturn(new Present<>(TimeZone.getTimeZone("UTC"))).when(mockData).data(eq(Tasks.TZ), any());

        assertThat(new EffectiveDueDate(mockData), is(AbsentMatcher.absent()));
    }


    @Test
    public void test_whenDueIsAbsent_startIsPresent_durationIsPresent_shouldUseStartPlusDuration()
    {
        long timestamp = System.currentTimeMillis();

        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(absent()).when(mockData).data(eq(Tasks.DUE), any());
        doReturn(new Present<>(timestamp)).when(mockData).data(eq(Tasks.DTSTART), any());
        doReturn(new Present<>(Duration.parse("PT2H"))).when(mockData).data(eq(Tasks.DURATION), any());
        doReturn(new Present<>(false)).when(mockData).data(eq(Tasks.IS_ALLDAY), any());
        doReturn(new Present<>(TimeZone.getTimeZone("Europe/Berlin"))).when(mockData).data(eq(Tasks.TZ), any());

        DateTime actual = new EffectiveDueDate(mockData).value();
        assertEquals(timestamp + TimeUnit.HOURS.toMillis(2), actual.getTimestamp());
        assertFalse(actual.isAllDay());
        assertEquals("Europe/Berlin", actual.getTimeZone().getID());
    }

}