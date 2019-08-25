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

package org.dmfs.opentaskspal.tasks;

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.rowdata.Composite;
import org.dmfs.android.contentpal.rowdata.DelegatingRowData;
import org.dmfs.android.contentpal.rowdata.Referring;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;

import androidx.annotation.NonNull;


/**
 * {@link RowData} of an instance override.
 *
 * @author Marten Gajda
 */
public final class OriginalInstanceData extends DelegatingRowData<TaskContract.Tasks>
{
    public OriginalInstanceData(@NonNull RowSnapshot originalTask, DateTime originalTime)
    {
        super(new Composite<>(
                new Referring<>(TaskContract.Tasks.ORIGINAL_INSTANCE_ID, originalTask),
                (transactionContext, builder) -> builder.withValue(TaskContract.Tasks.ORIGINAL_INSTANCE_TIME, originalTime.getTimestamp())));
    }

}
