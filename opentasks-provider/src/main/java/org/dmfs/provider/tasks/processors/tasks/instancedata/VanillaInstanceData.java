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

import org.dmfs.jems.single.Single;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A {@link Single} of instance data {@link ContentValues}. It initializes most columns with {@code null} values, except for {@link
 * TaskContract.Instances#TASK_ID} which is left out and {@link TaskContract.Instances#DISTANCE_FROM_CURRENT} which is initialized with {@code 0} as well.
 *
 * @author Marten Gajda
 */
public final class VanillaInstanceData implements Single<ContentValues>
{
    @Override
    public ContentValues value()
    {
        ContentValues values = new ContentValues(10);
        values.putNull(TaskContract.Instances.INSTANCE_START);
        values.putNull(TaskContract.Instances.INSTANCE_START_SORTING);
        values.putNull(TaskContract.Instances.INSTANCE_DUE);
        values.putNull(TaskContract.Instances.INSTANCE_DUE_SORTING);
        values.putNull(TaskContract.Instances.INSTANCE_DURATION);
        values.put(TaskContract.Instances.DISTANCE_FROM_CURRENT, 0);
        values.putNull(TaskContract.Instances.INSTANCE_ORIGINAL_TIME);
        return values;
    }
}
