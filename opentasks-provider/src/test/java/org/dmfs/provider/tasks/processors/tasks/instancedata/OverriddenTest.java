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

import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.provider.tasks.utils.ContentValuesWithLong;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.dmfs.optional.Absent.absent;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;


/**
 * @author Marten Gajda
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class OverriddenTest
{
    @Test
    public void testAbsent()
    {
        ContentValues instanceData = new Overridden(absent(), ContentValues::new).value();
        assertThat(instanceData, new ContentValuesWithLong(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, nullValue(Long.class)));
        assertThat(instanceData.size(), is(0));
    }


    @Test
    public void testAbsentWithStart()
    {
        ContentValues values = new ContentValues();
        values.put(TaskContract.Instances.INSTANCE_START, 10);

        ContentValues instanceData = new Overridden(absent(), () -> new ContentValues(values)).value();
        assertThat(instanceData, new ContentValuesWithLong(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, nullValue(Long.class)));
        assertThat(instanceData.size(), is(1));
    }


    @Test
    public void testAbsentWithDue()
    {
        ContentValues values = new ContentValues();
        values.put(TaskContract.Instances.INSTANCE_DUE, 20);

        ContentValues instanceData = new Overridden(absent(), () -> new ContentValues(values)).value();
        assertThat(instanceData, new ContentValuesWithLong(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, nullValue(Long.class)));
        assertThat(instanceData.size(), is(1));
    }


    @Test
    public void testAbsentWithStartAndDue()
    {
        ContentValues values = new ContentValues();
        values.put(TaskContract.Instances.INSTANCE_START, 10);
        values.put(TaskContract.Instances.INSTANCE_DUE, 20);

        ContentValues instanceData = new Overridden(absent(), () -> new ContentValues(values)).value();
        assertThat(instanceData, new ContentValuesWithLong(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, nullValue(Long.class)));
        assertThat(instanceData.size(), is(2));
    }


    @Test
    public void testPresent()
    {

        ContentValues instanceData = new Overridden(new Present<>(new DateTime(40)), ContentValues::new).value();
        assertThat(instanceData, new ContentValuesWithLong(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, 40));
        assertThat(instanceData.size(), is(1));
    }


    @Test
    public void testPresentWithStartAndDue()
    {
        ContentValues values = new ContentValues();
        values.put(TaskContract.Instances.INSTANCE_START, 10);
        values.put(TaskContract.Instances.INSTANCE_DUE, 20);

        ContentValues instanceData = new Overridden(new Present<>(new DateTime(40)), () -> new ContentValues(values)).value();
        assertThat(instanceData, new ContentValuesWithLong(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, 40));
        assertThat(instanceData.size(), is(3));
    }
}