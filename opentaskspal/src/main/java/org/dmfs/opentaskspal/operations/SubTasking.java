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

package org.dmfs.opentaskspal.operations;

import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.batches.DelegatingOperationsBatch;
import org.dmfs.android.contentpal.batches.MultiBatch;
import org.dmfs.android.contentpal.operations.Insert;
import org.dmfs.android.contentpal.operations.Put;
import org.dmfs.opentaskspal.tables.RelationsTable;
import org.dmfs.opentaskspal.tasks.Parent;
import org.dmfs.opentaskspal.tasks.SubTaskRelation;
import org.dmfs.tasks.contract.TaskContract;


/**
 * @author Gabor Keszthelyi
 */
public final class SubTasking extends DelegatingOperationsBatch
{

    public SubTasking(String authority, RowSnapshot<TaskContract.Tasks> subtask, RowSnapshot<TaskContract.Tasks> parentTask)
    {
        super(new MultiBatch(
                new Put<>(subtask, new Parent(parentTask)),
                new Insert<>(new RelationsTable(authority), new SubTaskRelation(subtask, parentTask))
        ));
    }
}
