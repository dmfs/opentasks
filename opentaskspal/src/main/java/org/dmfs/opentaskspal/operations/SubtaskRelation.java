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

import android.content.ContentProviderOperation;
import androidx.annotation.NonNull;

import org.dmfs.android.contentpal.InsertOperation;
import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.SoftRowReference;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.android.contentpal.operations.Insert;
import org.dmfs.jems.optional.Optional;
import org.dmfs.opentaskspal.tables.PropertiesTable;
import org.dmfs.opentaskspal.tasks.ParentTaskRelationData;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link InsertOperation} for adding a 'subtask relation' property to {@link TaskContract.Properties} table.
 *
 * @author Gabor Keszthelyi
 */
public final class SubtaskRelation implements InsertOperation<TaskContract.Properties>
{

    private final Operation<TaskContract.Properties> mDelegate;


    public SubtaskRelation(String authority, RowSnapshot<TaskContract.Tasks> subtask, RowSnapshot<TaskContract.Tasks> parentTask)
    {
        mDelegate = new Insert<>(new PropertiesTable(authority), new ParentTaskRelationData(parentTask, subtask));
    }


    @NonNull
    @Override
    public Optional<SoftRowReference<TaskContract.Properties>> reference()
    {
        return mDelegate.reference();
    }


    @NonNull
    @Override
    public ContentProviderOperation.Builder contentOperationBuilder(@NonNull TransactionContext transactionContext) throws UnsupportedOperationException
    {
        return mDelegate.contentOperationBuilder(transactionContext);
    }
}
