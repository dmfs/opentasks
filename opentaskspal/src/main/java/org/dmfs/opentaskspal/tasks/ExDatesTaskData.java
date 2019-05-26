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

package org.dmfs.opentaskspal.tasks;

import androidx.annotation.NonNull;

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.rowdata.DelegatingRowData;
import org.dmfs.opentaskspal.rowdata.DateTimeListData;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link RowData} for tasks with EXDATEs
 * <p>
 * TODO: how to make sure this is only ever used with tasks having a start and/or due date?
 *
 * @author Marten Gajda
 */
public final class ExDatesTaskData extends DelegatingRowData<TaskContract.Tasks>
{
    public ExDatesTaskData(@NonNull Iterable<DateTime> exdates)
    {
        super(new DateTimeListData<>(TaskContract.Tasks.EXDATE, exdates));
    }
}
