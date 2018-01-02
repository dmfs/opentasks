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
import org.dmfs.jems.single.decorators.DelegatingSingle;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A {@link Single} of the instance distance {@link ContentValues} of an instance.
 *
 * @author Marten Gajda
 */
public final class Distant extends DelegatingSingle<ContentValues>
{

    public Distant(int distance, Single<ContentValues> delegate)
    {
        super(() ->
        {
            ContentValues values = delegate.value();
            values.put(TaskContract.Instances.DISTANCE_FROM_CURRENT, distance);
            return values;
        });
    }
}
