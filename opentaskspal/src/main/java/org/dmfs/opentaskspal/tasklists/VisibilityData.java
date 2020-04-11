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

package org.dmfs.opentaskspal.tasklists;

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.rowdata.DelegatingRowData;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link RowData} of the visibility of a task list.
 *
 * @author Marten Gajda
 */
public final class VisibilityData extends DelegatingRowData<TaskContract.TaskLists>
{

    public VisibilityData()
    {
        this(true);
    }


    public VisibilityData(boolean visible)
    {
        super((transactionContext, builder) -> builder.withValue(TaskContract.TaskLists.VISIBLE, visible ? 1 : 0));
    }

}