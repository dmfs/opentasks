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
import org.dmfs.android.contentpal.projections.Composite;
import org.dmfs.android.contentpal.projections.MultiProjection;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.decorators.DelegatingOptional;
import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Tasks;


/**
 * An {@link Optional} of a specific {@link DateTime} value of a task.
 *
 * @author Marten Gajda
 * @author Gabor Keszthelyi
 */
public final class TaskDateTime extends DelegatingOptional<DateTime>
{
    public static final Projection<? super TaskContract.TaskColumns> PROJECTION = new Composite<>(
            new MultiProjection<>(Tasks.DTSTART, Tasks.DUE, Tasks.COMPLETED, Tasks.IS_ALLDAY),
            EffectiveTimezone.PROJECTION);


    public TaskDateTime(@NonNull String columnName, @NonNull final RowDataSnapshot<? extends TaskContract.TaskColumns> rowData)
    {
        super(new Mapped<>(
                (Long timeStamp) -> new Backed<>(rowData.data(Tasks.IS_ALLDAY, "1"::equals), false).value() ?
                        new DateTime(timeStamp).toAllDay() :
                        new DateTime(timeStamp).shiftTimeZone(new EffectiveTimezone(rowData).value()),
                rowData.data(columnName, Long::valueOf)));
    }
}
