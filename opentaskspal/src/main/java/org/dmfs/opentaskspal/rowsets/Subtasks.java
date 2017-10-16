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

package org.dmfs.opentaskspal.rowsets;

import android.content.ContentProviderClient;
import android.support.annotation.NonNull;

import org.dmfs.android.contentpal.RowSet;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.predicates.ReferringTo;
import org.dmfs.android.contentpal.rowsets.DelegatingRowSet;
import org.dmfs.android.contentpal.rowsets.QueryRowSet;
import org.dmfs.opentaskspal.views.TasksView;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link RowSet} for the subtasks of a given task.
 *
 * @author Gabor Keszthelyi
 */
public final class Subtasks extends DelegatingRowSet<TaskContract.Tasks>
{

    public Subtasks(String authority, ContentProviderClient client, @NonNull RowSnapshot<TaskContract.Tasks> parentTask)
    {
        super(new QueryRowSet<>(new TasksView(authority, client), new ReferringTo<>(TaskContract.Tasks.PARENT_ID, parentTask)));
    }

}
