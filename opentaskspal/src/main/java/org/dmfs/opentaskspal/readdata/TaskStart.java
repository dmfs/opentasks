/*
 * Copyright 2019 dmfs GmbH
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
import org.dmfs.android.contentpal.projections.Composite;
import org.dmfs.android.contentpal.projections.SingleColProjection;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.decorators.DelegatingOptional;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Tasks;


/**
 * {@link Optional} representing the start date of a task, if present.
 *
 * @author Marten Gajda
 */
public final class TaskStart extends DelegatingOptional<DateTime>
{
    public static final Projection<? super TaskContract.TaskColumns> PROJECTION =
            new Composite<>(new SingleColProjection<>(Tasks.DTSTART), TaskDateTime.PROJECTION);


    public TaskStart(@NonNull RowDataSnapshot<? extends TaskContract.TaskColumns> rowDataSnapshot)
    {
        super(new TaskDateTime(Tasks.DTSTART, rowDataSnapshot));
    }
}
