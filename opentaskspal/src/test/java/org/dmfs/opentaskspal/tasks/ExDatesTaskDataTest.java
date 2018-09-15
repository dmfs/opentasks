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

import org.dmfs.iterables.SingletonIterable;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.dmfs.android.contentpal.testing.contentoperationbuilder.WithValues.withValuesOnly;
import static org.dmfs.android.contentpal.testing.contentvalues.Containing.containing;
import static org.dmfs.android.contentpal.testing.contentvalues.NullValue.withNullValue;
import static org.dmfs.android.contentpal.testing.rowdata.RowDataMatcher.builds;
import static org.dmfs.iterables.EmptyIterable.instance;
import static org.junit.Assert.assertThat;


/**
 * @author Marten Gajda
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ExDatesTaskDataTest
{
    @Test
    public void testEmpty()
    {
        assertThat(new ExDatesTaskData(instance()),
                builds(
                        withValuesOnly(
                                withNullValue(TaskContract.Tasks.EXDATE))));
    }


    @Test
    public void testSingle()
    {
        assertThat(new ExDatesTaskData(new SingletonIterable<>(DateTime.parse("Europe/Berlin", "20171212T123456"))),
                builds(
                        withValuesOnly(
                                containing(TaskContract.Tasks.EXDATE, "20171212T113456Z"))));
    }


    @Test
    public void testSingleFloating()
    {
        assertThat(new ExDatesTaskData(new SingletonIterable<>(DateTime.parse("20171212T123456"))),
                builds(
                        withValuesOnly(
                                containing(TaskContract.Tasks.EXDATE, "20171212T123456"))));
    }


    @Test
    public void testSingleAllDay()
    {
        assertThat(new ExDatesTaskData(new SingletonIterable<>(DateTime.parse("20171212"))),
                builds(
                        withValuesOnly(
                                containing(TaskContract.Tasks.EXDATE, "20171212"))));
    }


    @Test
    public void testMulti1()
    {
        assertThat(new ExDatesTaskData(
                        new Seq<>(
                                DateTime.parse("Europe/Berlin", "20171212T123456"),
                                DateTime.parse("UTC", "20171213T123456"))),
                builds(
                        withValuesOnly(
                                containing(TaskContract.Tasks.EXDATE, "20171212T113456Z,20171213T123456Z"))));
    }


    @Test
    public void testMulti2()
    {
        assertThat(new ExDatesTaskData(
                        new Seq<>(
                                DateTime.parse("Europe/Berlin", "20171212T123456"),
                                DateTime.parse("UTC", "20171213T123456"),
                                DateTime.parse("America/New_York", "20171214T123456"))),
                builds(
                        withValuesOnly(
                                containing(TaskContract.Tasks.EXDATE, "20171212T113456Z,20171213T123456Z,20171214T173456Z"))));
    }

}