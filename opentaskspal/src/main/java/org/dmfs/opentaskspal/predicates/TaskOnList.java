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

package org.dmfs.opentaskspal.predicates;

import android.provider.BaseColumns;

import org.dmfs.android.contentpal.Predicate;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.predicates.AllOf;
import org.dmfs.android.contentpal.predicates.DelegatingPredicate;
import org.dmfs.android.contentpal.predicates.EqArg;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link Predicate} decorator that adds selection for tasks having the task list referred to by the given {@link RowSnapshot}.
 *
 * @author Gabor Keszthelyi
 */
public final class TaskOnList extends DelegatingPredicate
{
    public TaskOnList(RowSnapshot<TaskContract.TaskLists> taskListRow, Predicate predicate)
    {
        super(new AllOf(predicate,

                /*
                TODO Potential issue here with the predicate for update, delete and assert operations
                See https://github.com/dmfs/ContentPal/pull/47#issuecomment-329470827
                One of these 2 pull requests may allow to solve this problem:
                https://github.com/dmfs/ContentPal/pull/51
                https://github.com/dmfs/ContentPal/pull/52
                */
                new EqArg(TaskContract.Tasks.LIST_ID, taskListRow.values().charData(BaseColumns._ID).value("-1"))));
    }

}
