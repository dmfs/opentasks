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

import org.dmfs.android.contentpal.Predicate;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.predicates.AllOf;
import org.dmfs.android.contentpal.predicates.DelegatingPredicate;
import org.dmfs.android.contentpal.predicates.ReferringTo;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link Predicate} decorator that adds selection for tasks having the task list referred to by the given {@link RowSnapshot}.
 *
 * @author Gabor Keszthelyi
 */
public final class TaskOnList extends DelegatingPredicate<TaskContract.Tasks>
{
    public TaskOnList(RowSnapshot<TaskContract.TaskLists> taskListRow, Predicate<? super TaskContract.Tasks> predicate)
    {
        super(new AllOf<>(predicate, new ReferringTo<>(TaskContract.Tasks.LIST_ID, taskListRow)));
    }

}
