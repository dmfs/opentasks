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
import org.dmfs.jems.hamcrest.matchers.AbsentMatcher;
import org.dmfs.optional.Present;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.dmfs.jems.mockito.doubles.TestDoubles.failingMock;
import static org.dmfs.optional.Absent.absent;
import static org.hamcrest.MatcherAssert.assertThat;
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
        doReturn(new Present<CharSequence>(String.valueOf(timestamp))).when(mockData).charData(Tasks.DUE);
        doReturn(new Present<CharSequence>("0")).when(mockData).charData(Tasks.IS_ALLDAY);
        doReturn(new Present<CharSequence>("UTC")).when(mockData).charData(Tasks.TZ);

        DateTime actual = new EffectiveDueDate(mockData).value();
        assertEquals(timestamp, actual.getTimestamp());
    }


    @Test
    public void test_whenDueIsAbsent_startIsAbsent_durationIsAbsent_shouldBeAbsent()
    {
        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(absent()).when(mockData).charData(Tasks.DUE);
        doReturn(absent()).when(mockData).charData(Tasks.DTSTART);
        doReturn(absent()).when(mockData).charData(Tasks.DURATION);
        doReturn(new Present<CharSequence>("0")).when(mockData).charData(Tasks.IS_ALLDAY);
        doReturn(new Present<CharSequence>("UTC")).when(mockData).charData(Tasks.TZ);

        assertThat(new EffectiveDueDate(mockData), AbsentMatcher.<DateTime>isAbsent());
    }


    @Test
    public void test_whenDueIsAbsent_startIsPresent_durationIsAbsent_shouldBeAbsent()
    {
        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(absent()).when(mockData).charData(Tasks.DUE);
        doReturn(new Present<CharSequence>("234234")).when(mockData).charData(Tasks.DTSTART);
        doReturn(absent()).when(mockData).charData(Tasks.DURATION);
        doReturn(new Present<CharSequence>("0")).when(mockData).charData(Tasks.IS_ALLDAY);
        doReturn(new Present<CharSequence>("UTC")).when(mockData).charData(Tasks.TZ);

        assertThat(new EffectiveDueDate(mockData), AbsentMatcher.<DateTime>isAbsent());
    }


    @Test
    public void test_whenDueIsAbsent_startIsAbsent_durationIsPresent_shouldBeAbsent()
    {
        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(absent()).when(mockData).charData(Tasks.DUE);
        doReturn(absent()).when(mockData).charData(Tasks.DTSTART);
        doReturn(new Present<CharSequence>("P7W")).when(mockData).charData(Tasks.DURATION);
        doReturn(new Present<CharSequence>("0")).when(mockData).charData(Tasks.IS_ALLDAY);
        doReturn(new Present<CharSequence>("UTC")).when(mockData).charData(Tasks.TZ);

        assertThat(new EffectiveDueDate(mockData), AbsentMatcher.<DateTime>isAbsent());
    }


    @Test
    public void test_whenDueIsAbsent_startIsPresent_durationIsPresent_shouldUseStartPlusDuration()
    {
        long timestamp = System.currentTimeMillis();

        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(absent()).when(mockData).charData(Tasks.DUE);
        doReturn(new Present<>(String.valueOf(timestamp))).when(mockData).charData(Tasks.DTSTART);
        doReturn(new Present<CharSequence>("PT2H")).when(mockData).charData(Tasks.DURATION);
        doReturn(new Present<CharSequence>("0")).when(mockData).charData(Tasks.IS_ALLDAY);
        doReturn(new Present<CharSequence>("Europe/Berlin")).when(mockData).charData(Tasks.TZ);

        DateTime actual = new EffectiveDueDate(mockData).value();
        assertEquals(timestamp + TimeUnit.HOURS.toMillis(2), actual.getTimestamp());
        assertFalse(actual.isAllDay());
        assertEquals("Europe/Berlin", actual.getTimeZone().getID());
    }

}