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

import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.decorators.DelegatingSingle;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A decorator to a {@link Single} of {@link ContentValues} adding due data.
 *
 * @author Marten Gajda
 */
public final class DueDated extends DelegatingSingle<ContentValues>
{
    public DueDated(Optional<DateTime> due, Single<ContentValues> delegate)
    {
        super(new Dated(due, TaskContract.Instances.INSTANCE_DUE, TaskContract.Instances.INSTANCE_DUE_SORTING, delegate));
    }
}
