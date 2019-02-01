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

import org.dmfs.android.bolts.color.elementary.ValueColor;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.junit.Test;

import static org.dmfs.jems.mockito.doubles.TestDoubles.failingMock;
import static org.dmfs.optional.Absent.absent;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;


/**
 * Unit test for {@link EffectiveTaskColor}.
 *
 * @author Gabor Keszthelyi
 */
public final class EffectiveTaskColorTest
{

    @Test
    public void test_whenTaskColorIsPresent_shouldReturnThat()
    {
        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(new Present<>(new ValueColor(123))).when(mockData).data(eq(Tasks.TASK_COLOR), any());
        doReturn(absent()).when(mockData).data(eq(Tasks.LIST_COLOR), any());

        assertThat(new EffectiveTaskColor(mockData).argb(), is(123));
    }


    @Test
    public void test_whenTaskColorIsAbsent_shouldReturnTaskListColor()
    {
        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(absent()).when(mockData).data(eq(Tasks.TASK_COLOR), any());
        doReturn(new Present<>(new ValueColor(567))).when(mockData).data(eq(Tasks.LIST_COLOR), any());

        assertThat(new EffectiveTaskColor(mockData).argb(), is(567));
    }


    @Test(expected = Exception.class)
    public void test_whenTaskColorAndListColorAreAbsent_shouldThrow()
    {
        RowDataSnapshot<Tasks> mockData = failingMock(RowDataSnapshot.class);
        doReturn(absent()).when(mockData).data(eq(Tasks.TASK_COLOR), any());
        doReturn(absent()).when(mockData).data(eq(Tasks.LIST_COLOR), any());

        new EffectiveTaskColor(mockData).argb();
    }

}