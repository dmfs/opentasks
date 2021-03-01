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

package org.dmfs.tasks.detailsscreen;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link SubtasksView.Params} that adapts the given {@link RowDataSnapshot}s (and takes the list color).
 *
 * @author Gabor Keszthelyi
 */
public final class RowDataSubtasksViewParams implements SubtasksView.Params
{
    private final Color mTaskListColor;
    private final Iterable<RowDataSnapshot<TaskContract.Tasks>> mSubtaskRows;


    public RowDataSubtasksViewParams(Color taskListColor, Iterable<RowDataSnapshot<TaskContract.Tasks>> subtaskRows)
    {
        mTaskListColor = taskListColor;
        mSubtaskRows = subtaskRows;
    }


    @Override
    public Color taskListColor()
    {
        return mTaskListColor;
    }


    @Override
    public Iterable<SubtaskView.Params> subtasks()
    {
        return new Mapped<>(RowDataSubtaskViewParams::new, mSubtaskRows);
    }
}
