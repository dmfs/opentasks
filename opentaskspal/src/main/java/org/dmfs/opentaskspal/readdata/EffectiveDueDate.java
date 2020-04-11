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

import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.projections.Composite;
import org.dmfs.android.contentpal.projections.MultiProjection;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.adapters.FirstPresent;
import org.dmfs.jems.optional.composite.Zipped;
import org.dmfs.jems.optional.decorators.DelegatingOptional;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Tasks;

import androidx.annotation.NonNull;


/**
 * {@link Optional} representing the effective due date of a task. It's either taken directly from the due value (if present) or from start + duration (if both
 * are present).
 *
 * @author Marten Gajda
 */
public final class EffectiveDueDate extends DelegatingOptional<DateTime>
{
    public static final Projection<? super TaskContract.TaskColumns> PROJECTION = new Composite<>(
            new MultiProjection<>(Tasks.DUE, Tasks.DTSTART),
            TaskDateTime.PROJECTION,
            TaskDuration.PROJECTION);


    public EffectiveDueDate(@NonNull RowDataSnapshot<? extends TaskContract.TaskColumns> rowDataSnapshot)
    {
        super(new FirstPresent<>(
                new Seq<>(
                        new TaskDateTime(Tasks.DUE, rowDataSnapshot),
                        new Zipped<>(
                                new TaskDateTime(Tasks.DTSTART, rowDataSnapshot),
                                new TaskDuration(rowDataSnapshot),
                                DateTime::addDuration))));
    }
}
