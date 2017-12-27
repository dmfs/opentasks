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

import org.dmfs.tasks.contract.TaskContract;
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
public class VanillaInstanceDataTest
{
    @Test
    public void testValue() throws Exception
    {
        ContentValues values = new VanillaInstanceData().value();
        assertThat(values.get(TaskContract.Instances.INSTANCE_START), nullValue());
        assertThat(values.get(TaskContract.Instances.INSTANCE_START_SORTING), nullValue());
        assertThat(values.get(TaskContract.Instances.INSTANCE_DUE), nullValue());
        assertThat(values.get(TaskContract.Instances.INSTANCE_DUE_SORTING), nullValue());
        assertThat(values.get(TaskContract.Instances.INSTANCE_DURATION), nullValue());
        assertThat(values.get(TaskContract.Instances.INSTANCE_STATUS), is(TaskContract.Instances.INSTANCE_STATUS_NEXT));
        assertThat(values.get(TaskContract.Instances.INSTANCE_ORIGINAL_TIME), is(0));
        assertThat(values.size(), is(7));
    }

}