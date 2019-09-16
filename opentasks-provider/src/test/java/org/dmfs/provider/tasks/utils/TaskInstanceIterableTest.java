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

package org.dmfs.provider.tasks.utils;

import android.content.ContentValues;

import org.dmfs.iterables.elementary.Seq;
import org.dmfs.provider.tasks.model.ContentValuesTaskAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.dmfs.jems.hamcrest.matchers.IterableMatcher.iteratesTo;
import static org.junit.Assert.assertThat;


/**
 * @author Marten Gajda
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TaskInstanceIterableTest
{
    @Test
    public void testAbsolute() throws Exception
    {
        TaskAdapter taskAdapter = new ContentValuesTaskAdapter(new ContentValues());
        taskAdapter.set(TaskAdapter.DTSTART, DateTime.parse("Europe/Berlin", "20170606T121314"));
        taskAdapter.set(TaskAdapter.RRULE, new RecurrenceRule("FREQ=DAILY;INTERVAL=2;COUNT=10"));

        assertThat(new TaskInstanceIterable(taskAdapter),
                iteratesTo(
                        DateTime.parse("Europe/Berlin", "20170606T121314"),
                        DateTime.parse("Europe/Berlin", "20170608T121314"),
                        DateTime.parse("Europe/Berlin", "20170610T121314"),
                        DateTime.parse("Europe/Berlin", "20170612T121314"),
                        DateTime.parse("Europe/Berlin", "20170614T121314"),
                        DateTime.parse("Europe/Berlin", "20170616T121314"),
                        DateTime.parse("Europe/Berlin", "20170618T121314"),
                        DateTime.parse("Europe/Berlin", "20170620T121314"),
                        DateTime.parse("Europe/Berlin", "20170622T121314"),
                        DateTime.parse("Europe/Berlin", "20170624T121314")
                ));
    }


    @Test
    public void testAllDay() throws Exception
    {
        TaskAdapter taskAdapter = new ContentValuesTaskAdapter(new ContentValues());
        taskAdapter.set(TaskAdapter.DTSTART, DateTime.parse("20170606"));
        taskAdapter.set(TaskAdapter.RRULE, new RecurrenceRule("FREQ=DAILY;INTERVAL=2;COUNT=10"));

        assertThat(new TaskInstanceIterable(taskAdapter),
                iteratesTo(
                        DateTime.parse("20170606"),
                        DateTime.parse("20170608"),
                        DateTime.parse("20170610"),
                        DateTime.parse("20170612"),
                        DateTime.parse("20170614"),
                        DateTime.parse("20170616"),
                        DateTime.parse("20170618"),
                        DateTime.parse("20170620"),
                        DateTime.parse("20170622"),
                        DateTime.parse("20170624")
                ));
    }


    @Test
    public void testFloating() throws Exception
    {
        TaskAdapter taskAdapter = new ContentValuesTaskAdapter(new ContentValues());
        taskAdapter.set(TaskAdapter.DTSTART, DateTime.parse("20170606T121314"));
        taskAdapter.set(TaskAdapter.RRULE, new RecurrenceRule("FREQ=DAILY;INTERVAL=2;COUNT=10"));

        assertThat(new TaskInstanceIterable(taskAdapter),
                iteratesTo(
                        DateTime.parse("20170606T121314"),
                        DateTime.parse("20170608T121314"),
                        DateTime.parse("20170610T121314"),
                        DateTime.parse("20170612T121314"),
                        DateTime.parse("20170614T121314"),
                        DateTime.parse("20170616T121314"),
                        DateTime.parse("20170618T121314"),
                        DateTime.parse("20170620T121314"),
                        DateTime.parse("20170622T121314"),
                        DateTime.parse("20170624T121314")
                ));
    }


    @Test
    public void testRDate() throws Exception
    {
        TaskAdapter taskAdapter = new ContentValuesTaskAdapter(new ContentValues());
        taskAdapter.set(TaskAdapter.DTSTART, DateTime.parse("Europe/Berlin", "20170606T121314"));
        taskAdapter.set(TaskAdapter.RDATE, new Seq<>(
                DateTime.parse("Europe/Berlin", "20170606T121314"),
                DateTime.parse("Europe/Berlin", "20170608T121314"),
                DateTime.parse("Europe/Berlin", "20170610T121314"),
                DateTime.parse("Europe/Berlin", "20170612T121314"),
                DateTime.parse("Europe/Berlin", "20170614T121314"),
                DateTime.parse("Europe/Berlin", "20170616T121314"),
                DateTime.parse("Europe/Berlin", "20170618T121314"),
                DateTime.parse("Europe/Berlin", "20170620T121314"),
                DateTime.parse("Europe/Berlin", "20170622T121314"),
                DateTime.parse("Europe/Berlin", "20170624T121314")
        ));

        assertThat(new TaskInstanceIterable(taskAdapter),
                iteratesTo(
                        DateTime.parse("Europe/Berlin", "20170606T121314"),
                        DateTime.parse("Europe/Berlin", "20170608T121314"),
                        DateTime.parse("Europe/Berlin", "20170610T121314"),
                        DateTime.parse("Europe/Berlin", "20170612T121314"),
                        DateTime.parse("Europe/Berlin", "20170614T121314"),
                        DateTime.parse("Europe/Berlin", "20170616T121314"),
                        DateTime.parse("Europe/Berlin", "20170618T121314"),
                        DateTime.parse("Europe/Berlin", "20170620T121314"),
                        DateTime.parse("Europe/Berlin", "20170622T121314"),
                        DateTime.parse("Europe/Berlin", "20170624T121314")
                ));
    }


    @Test
    public void testRDateAndRRule() throws Exception
    {
        TaskAdapter taskAdapter = new ContentValuesTaskAdapter(new ContentValues());
        taskAdapter.set(TaskAdapter.DTSTART, DateTime.parse("Europe/Berlin", "20170606T121314"));
        taskAdapter.set(TaskAdapter.RRULE, new RecurrenceRule("FREQ=DAILY;INTERVAL=2;COUNT=10"));
        taskAdapter.set(TaskAdapter.RDATE, new Seq<>(
                DateTime.parse("Europe/Berlin", "20170606T121313"),
                DateTime.parse("Europe/Berlin", "20170608T121313"),
                DateTime.parse("Europe/Berlin", "20170610T121313"),
                DateTime.parse("Europe/Berlin", "20170612T121313"),
                DateTime.parse("Europe/Berlin", "20170614T121313"),
                DateTime.parse("Europe/Berlin", "20170616T121313"),
                DateTime.parse("Europe/Berlin", "20170618T121313"),
                DateTime.parse("Europe/Berlin", "20170620T121313"),
                DateTime.parse("Europe/Berlin", "20170622T121313"),
                DateTime.parse("Europe/Berlin", "20170624T121313")
        ));

        assertThat(new TaskInstanceIterable(taskAdapter),
                iteratesTo(
                        DateTime.parse("Europe/Berlin", "20170606T121313"),
                        DateTime.parse("Europe/Berlin", "20170606T121314"),
                        DateTime.parse("Europe/Berlin", "20170608T121313"),
                        DateTime.parse("Europe/Berlin", "20170608T121314"),
                        DateTime.parse("Europe/Berlin", "20170610T121313"),
                        DateTime.parse("Europe/Berlin", "20170610T121314"),
                        DateTime.parse("Europe/Berlin", "20170612T121313"),
                        DateTime.parse("Europe/Berlin", "20170612T121314"),
                        DateTime.parse("Europe/Berlin", "20170614T121313"),
                        DateTime.parse("Europe/Berlin", "20170614T121314"),
                        DateTime.parse("Europe/Berlin", "20170616T121313"),
                        DateTime.parse("Europe/Berlin", "20170616T121314"),
                        DateTime.parse("Europe/Berlin", "20170618T121313"),
                        DateTime.parse("Europe/Berlin", "20170618T121314"),
                        DateTime.parse("Europe/Berlin", "20170620T121313"),
                        DateTime.parse("Europe/Berlin", "20170620T121314"),
                        DateTime.parse("Europe/Berlin", "20170622T121313"),
                        DateTime.parse("Europe/Berlin", "20170622T121314"),
                        DateTime.parse("Europe/Berlin", "20170624T121313"),
                        DateTime.parse("Europe/Berlin", "20170624T121314")
                ));
    }
}