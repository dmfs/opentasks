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

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.rowdata.CharSequenceRowData;
import org.dmfs.android.contentpal.rowdata.Composite;
import org.dmfs.android.contentpal.rowdata.DelegatingRowData;
import org.dmfs.android.contentpal.rowdata.RawRowData;
import org.dmfs.android.contentpal.rowdata.Referring;
import org.dmfs.tasks.contract.TaskContract;

import androidx.annotation.NonNull;


/**
 * {@link RowData} for adding a {@link TaskContract.Property.Relation} property.
 *
 * @author Gabor Keszthelyi
 */
public final class RelationData extends DelegatingRowData<TaskContract.Properties>
{
    public RelationData(@NonNull RowSnapshot<TaskContract.Tasks> relatingTask,
                        int relType,
                        @NonNull RowSnapshot<TaskContract.Tasks> relatedTask)
    {
        super(new PropertyData(TaskContract.Property.Relation.CONTENT_ITEM_TYPE,
                new Composite<>(
                        new Referring<>(TaskContract.Property.Relation.TASK_ID, relatingTask),
                        new RawRowData<>(TaskContract.Property.Relation.RELATED_TYPE, relType),
                        new Referring<>(TaskContract.Property.Relation.RELATED_ID, relatedTask))));
    }


    public RelationData(@NonNull RowSnapshot<TaskContract.Tasks> relatingTask,
                        int relType,
                        @NonNull CharSequence relatedTaskUid)
    {
        super(new PropertyData(TaskContract.Property.Relation.CONTENT_ITEM_TYPE,
                new Composite<>(
                        new Referring<>(TaskContract.Property.Relation.TASK_ID, relatingTask),
                        new RawRowData<>(TaskContract.Property.Relation.RELATED_TYPE, relType),
                        new CharSequenceRowData<>(TaskContract.Property.Relation.RELATED_UID, relatedTaskUid))));
    }
}
