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

import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.dmfs.jems.mockito.doubles.TestDoubles.failingMock;
import static org.dmfs.optional.Absent.absent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;


/**
 * Unit test for {@link RowSnapshotComposedTaskDateTime}.
 *
 * @author Gabor Keszthelyi
 */
public final class RowSnapshotCombinedDateTimeTest
{

    @Test
    public void test_whenColumnValueIsAbsent_shouldBeAbsent()
    {
        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(absent()).when(mockData).data(eq(Tasks.DTSTART), any());
        doReturn(absent()).when(mockData).data(eq(Tasks.TZ), any());

        assertThat(new RowSnapshotComposedTaskDateTime(Tasks.DTSTART, mockData), AbsentMatcher.<DateTime>isAbsent());
    }


    @Test
    public void test_whenIsAllDayIsPresentAndTrue_shouldReturnAllDayDateTime()
    {
        long timeStamp = System.currentTimeMillis();

        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(new Present<>(timeStamp)).when(mockData).data(eq(Tasks.DTSTART), any());
        doReturn(new Present<>(true)).when(mockData).data(eq(Tasks.IS_ALLDAY), any());
        doReturn(new Present<>(TimeZone.getTimeZone("UTC"))).when(mockData).data(eq(Tasks.TZ), any());

        DateTime actual = new RowSnapshotComposedTaskDateTime(Tasks.DTSTART, mockData).value();
        assertTrue(actual.isAllDay());
        assertEquals(new DateTime(timeStamp).toAllDay(), actual);
    }


    @Test
    public void test_whenIsAllDayIsPresentAndFalse_shouldReturnNotAllDayDateTime()
    {
        long timeStamp = System.currentTimeMillis();

        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(new Present<>(timeStamp)).when(mockData).data(eq(Tasks.DTSTART), any());
        doReturn(new Present<>(false)).when(mockData).data(eq(Tasks.IS_ALLDAY), any());
        doReturn(new Present<>(TimeZone.getTimeZone("UTC"))).when(mockData).data(eq(Tasks.TZ), any());

        DateTime actual = new RowSnapshotComposedTaskDateTime(Tasks.DTSTART, mockData).value();
        assertFalse(actual.isAllDay());
        assertEquals(timeStamp, actual.getTimestamp());
    }


    @Test
    public void test_whenIsAllDayIsFalse_shouldReturnDateTimeWithTimeZoneShifted()
    {
        long timeStamp = System.currentTimeMillis();

        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(new Present<>(timeStamp)).when(mockData).data(eq(Tasks.DTSTART), any());
        doReturn(new Present<>(false)).when(mockData).data(eq(Tasks.IS_ALLDAY), any());
        doReturn(new Present<>(TimeZone.getTimeZone("Europe/Berlin"))).when(mockData).data(eq(Tasks.TZ), any());

        DateTime actual = new RowSnapshotComposedTaskDateTime(Tasks.DTSTART, mockData).value();
        assertFalse(actual.isAllDay());
        assertEquals(timeStamp, actual.getTimestamp());
        assertEquals("Europe/Berlin", actual.getTimeZone().getID());
    }

}