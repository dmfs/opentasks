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

package org.dmfs.provider.tasks.processors.tasks.instancedata;

import android.content.ContentValues;

import org.dmfs.optional.Present;
import org.dmfs.provider.tasks.utils.ContentValuesWithLong;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.TimeZone;

import static org.dmfs.optional.Absent.absent;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;


/**
 * @author Marten Gajda
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class StartDatedTest
{

    @Test
    public void testNone() throws Exception
    {
        ContentValues instanceData = new StartDated(absent(), ContentValues::new).value();

        assertThat(instanceData, new ContentValuesWithLong(TaskContract.Instances.INSTANCE_START, nullValue(Long.class)));
        assertThat(instanceData, new ContentValuesWithLong(TaskContract.Instances.INSTANCE_START_SORTING, nullValue(Long.class)));
        // this doesn't actually add anything, the ContentValues are expected to contain null values.
        assertThat(instanceData.size(), is(0));
    }


    @Test
    public void testStartEurope() throws Exception
    {
        DateTime start = DateTime.parse("Europe/Berlin", "20171208T125500");

        ContentValues instanceData = new StartDated(new Present<>(start), ContentValues::new).value();

        assertThat(instanceData, new ContentValuesWithLong(TaskContract.Instances.INSTANCE_START, start.getTimestamp()));
        assertThat(instanceData,
                new ContentValuesWithLong(TaskContract.Instances.INSTANCE_START_SORTING, start.shiftTimeZone(TimeZone.getDefault()).getInstance()));
        assertThat(instanceData.size(), is(2));
    }


    @Test
    public void testStartAmerica() throws Exception
    {
        DateTime start = DateTime.parse("America/New_York", "20171208T125500");

        ContentValues instanceData = new StartDated(new Present<>(start), ContentValues::new).value();

        assertThat(instanceData, new ContentValuesWithLong(TaskContract.Instances.INSTANCE_START, start.getTimestamp()));
        assertThat(instanceData,
                new ContentValuesWithLong(TaskContract.Instances.INSTANCE_START_SORTING, start.shiftTimeZone(TimeZone.getDefault()).getInstance()));
        assertThat(instanceData.size(), is(2));
    }
}