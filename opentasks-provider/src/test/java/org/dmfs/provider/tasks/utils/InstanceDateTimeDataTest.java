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

import org.dmfs.optional.Absent;
import org.dmfs.optional.Present;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.contract.TaskContract;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;


/**
 * @author Marten Gajda
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class InstanceDateTimeDataTest
{

    @Test
    public void testNone() throws Exception
    {
        ContentValues instanceData = new InstanceDateTimeData(Absent.<DateTime>absent(), Absent.<DateTime>absent(), Absent.<Duration>absent(),
                Absent.<DateTime>absent()).value();

        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START, nullValue(Long.class)));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START_SORTING, nullValue(Long.class)));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE, nullValue(Long.class)));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE_SORTING, nullValue(Long.class)));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DURATION, nullValue(Long.class)));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, 0));
    }


    @Test
    public void testStart() throws Exception
    {
        DateTime start = DateTime.parse("Europe/Berlin", "20171208T125500");

        ContentValues instanceData = new InstanceDateTimeData(new Present<>(start), Absent.<DateTime>absent(), Absent.<Duration>absent(),
                new Present<>(start)).value();

        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START, start.getTimestamp()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START_SORTING, start.getInstance()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE, nullValue(Long.class)));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE_SORTING, nullValue(Long.class)));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DURATION, nullValue(Long.class)));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, start.getTimestamp()));
    }


    @Test
    public void testStartDuration() throws Exception
    {
        DateTime start = DateTime.parse("Europe/Berlin", "20171208T125500");
        DateTime due = DateTime.parse("Europe/Berlin", "20171208T155500");
        Duration duration = Duration.parse("PT3H");

        ContentValues instanceData = new InstanceDateTimeData(new Present<>(start), Absent.<DateTime>absent(), new Present<>(duration),
                new Present<>(start)).value();

        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START, start.getTimestamp()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START_SORTING, start.getInstance()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE, due.getTimestamp()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE_SORTING, due.getInstance()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DURATION, duration.toMillis()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, start.getTimestamp()));
    }


    @Test
    public void testStartDue() throws Exception
    {
        DateTime start = DateTime.parse("Europe/Berlin", "20171208T125500");
        DateTime due = DateTime.parse("Europe/Berlin", "20171208T155500");
        Duration duration = Duration.parse("PT3H");

        ContentValues instanceData = new InstanceDateTimeData(new Present<>(start), new Present<>(due), Absent.<Duration>absent(),
                new Present<>(start)).value();

        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START, start.getTimestamp()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START_SORTING, start.getInstance()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE, due.getTimestamp()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE_SORTING, due.getInstance()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DURATION, duration.toMillis()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, start.getTimestamp()));
    }


    @Test
    public void testStartDueOriginal() throws Exception
    {
        DateTime start = DateTime.parse("Europe/Berlin", "20171208T125500");
        DateTime due = DateTime.parse("Europe/Berlin", "20171208T155500");
        Duration duration = Duration.parse("PT3H");
        DateTime original = DateTime.parse("Europe/Berlin", "20171210T155500");

        ContentValues instanceData = new InstanceDateTimeData(new Present<>(start), new Present<>(due), Absent.<Duration>absent(),
                new Present<>(original)).value();

        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START, start.getTimestamp()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START_SORTING, start.getInstance()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE, due.getTimestamp()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE_SORTING, due.getInstance()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DURATION, duration.toMillis()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, original.getTimestamp()));
    }


    @Test
    public void testStartDueAbsentOriginal() throws Exception
    {
        DateTime start = DateTime.parse("Europe/Berlin", "20171208T125500");
        DateTime due = DateTime.parse("Europe/Berlin", "20171208T155500");
        Duration duration = Duration.parse("PT3H");

        ContentValues instanceData = new InstanceDateTimeData(new Present<>(start), new Present<>(due), Absent.<Duration>absent(),
                Absent.<DateTime>absent()).value();

        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START, start.getTimestamp()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_START_SORTING, start.getInstance()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE, due.getTimestamp()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DUE_SORTING, due.getInstance()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_DURATION, duration.toMillis()));
        assertThat(instanceData, new Contains(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, start.getTimestamp()));
    }


    /**
     * A {@link Matcher} to test if {@link ContentValues} contain a specific Long value.
     * <p>
     * TODO: can we convert that into a more generic {@link ContentValues} matcher? It might be useful in other places.
     */
    private final class Contains extends FeatureMatcher<ContentValues, Long>
    {
        private final String mKey;


        public Contains(String valueKey, long value)
        {
            this(valueKey, is(value));
        }


        public Contains(String valueKey, Matcher<Long> matcher)
        {
            super(matcher, "Long value " + valueKey, "Long value " + valueKey);
            mKey = valueKey;
        }


        @Override
        protected Long featureValueOf(ContentValues actual)
        {
            return actual.getAsLong(mKey);
        }
    }
}