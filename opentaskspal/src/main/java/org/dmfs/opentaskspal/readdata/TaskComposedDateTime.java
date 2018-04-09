/*
 * Copyright 2018 dmfs GmbH
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

import android.support.annotation.NonNull;

import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.projections.MultiProjection;
import org.dmfs.optional.decorators.DelegatingOptional;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract.Tasks;


/**
 * Composed date-time of a task corresponding to the given timestamp column in the provided {@link RowDataSnapshot}.
 *
 * @author Gabor Keszthelyi
 */
public final class TaskComposedDateTime extends DelegatingOptional<DateTime>
{
    public static final Projection<Tasks> PROJECTION = new MultiProjection<>(Tasks.TZ, Tasks.IS_ALLDAY);


    public TaskComposedDateTime(@NonNull String timestampColumn, @NonNull RowDataSnapshot<Tasks> rowDataSnapshot)
    {
        super(new RowDataSnapshotComposedDateTime(rowDataSnapshot, timestampColumn, Tasks.TZ, Tasks.IS_ALLDAY));
    }
}
