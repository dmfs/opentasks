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

package org.dmfs.opentaskspal.readdata;

import androidx.annotation.NonNull;

import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.projections.SingleColProjection;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.decorators.DelegatingOptional;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Tasks;


/**
 * The {@link Optional} {@link Duration} of a task {@link RowDataSnapshot}.
 *
 * @author Marten Gajda
 */
public final class TaskDuration extends DelegatingOptional<Duration>
{
    public static final Projection<? super TaskContract.TaskColumns> PROJECTION = new SingleColProjection<>(Tasks.DURATION);


    public TaskDuration(@NonNull RowDataSnapshot<? extends TaskContract.TaskColumns> rowData)
    {
        super(rowData.data(Tasks.DURATION, Duration::parse));
    }
}
