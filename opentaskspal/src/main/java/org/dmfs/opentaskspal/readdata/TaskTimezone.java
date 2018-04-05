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

import android.support.annotation.NonNull;

import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.projections.SingleColProjection;
import org.dmfs.optional.Optional;
import org.dmfs.optional.decorators.DelegatingOptional;
import org.dmfs.tasks.contract.TaskContract.Tasks;

import java.util.TimeZone;


/**
 * {@link Optional} for the stored {@link TimeZone} of a task.
 *
 * @author Marten Gajda
 * @author Gabor Keszthelyi
 */
public final class TaskTimezone extends DelegatingOptional<TimeZone>
{
    public static final Projection<Tasks> PROJECTION = new SingleColProjection<>(Tasks.TZ);


    public TaskTimezone(@NonNull RowDataSnapshot<Tasks> rowData)
    {
        super(rowData.data(Tasks.TZ, TimeZone::getTimeZone));
    }

}
