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

import org.dmfs.provider.tasks.utils.ContentValuesWithLong;
import org.dmfs.tasks.contract.TaskContract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.dmfs.jems.hamcrest.matchers.SingleMatcher.hasValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;


/**
 * @author Marten Gajda
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class EnduringTest
{
    @Test
    public void testNoValue()
    {
        assertThat(new Enduring(ContentValues::new), hasValue(new ContentValuesWithLong(TaskContract.Instances.INSTANCE_DURATION, nullValue(Long.class))));
        assertThat(new Enduring(ContentValues::new).value().size(), is(1));
    }


    @Test
    public void testStartValue()
    {
        ContentValues values = new ContentValues(1);
        values.put(TaskContract.Instances.INSTANCE_START, 10);
        assertThat(new Enduring(() -> new ContentValues(values)),
                hasValue(new ContentValuesWithLong(TaskContract.Instances.INSTANCE_DURATION, nullValue(Long.class))));
        assertThat(new Enduring(() -> new ContentValues(values)).value().size(), is(2));
    }


    @Test
    public void testDueValue()
    {
        ContentValues values = new ContentValues(1);
        values.put(TaskContract.Instances.INSTANCE_DUE, 10);
        assertThat(new Enduring(() -> new ContentValues(values)),
                hasValue(new ContentValuesWithLong(TaskContract.Instances.INSTANCE_DURATION, nullValue(Long.class))));
        assertThat(new Enduring(() -> new ContentValues(values)).value().size(), is(2));
    }


    @Test
    public void testStartDueValue()
    {
        ContentValues values = new ContentValues(2);
        values.put(TaskContract.Instances.INSTANCE_START, 1);
        values.put(TaskContract.Instances.INSTANCE_DUE, 10);
        assertThat(new Enduring(() -> new ContentValues(values)), hasValue(new ContentValuesWithLong(TaskContract.Instances.INSTANCE_DURATION, 9)));
        assertThat(new Enduring(() -> new ContentValues(values)).value().size(), is(3));
    }
}