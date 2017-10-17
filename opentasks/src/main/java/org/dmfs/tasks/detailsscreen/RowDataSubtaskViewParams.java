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
import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.projections.Composite;
import org.dmfs.jems.optional.Optional;
import org.dmfs.opentaskspal.readdata.EffectiveDueDate;
import org.dmfs.opentaskspal.readdata.EffectiveTaskColor;
import org.dmfs.opentaskspal.readdata.Id;
import org.dmfs.opentaskspal.readdata.PercentComplete;
import org.dmfs.opentaskspal.readdata.TaskTitle;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link SubtasksView.Params} that reads the data from the given {@link RowDataSnapshot}.
 *
 * @author Gabor Keszthelyi
 */
public final class RowDataSubtaskViewParams implements SubtaskView.Params
{

    /**
     * The projection required for this adapter to work.
     */
    public static final Projection<TaskContract.Tasks> SUBTASK_PROJECTION = new Composite<>(
            Id.PROJECTION,
            TaskTitle.PROJECTION,
            EffectiveDueDate.PROJECTION,
            EffectiveTaskColor.PROJECTION,
            PercentComplete.PROJECTION
    );

    private final RowDataSnapshot<TaskContract.Tasks> mRowDataSnapshot;


    public RowDataSubtaskViewParams(RowDataSnapshot<TaskContract.Tasks> rowDataSnapshot)
    {
        mRowDataSnapshot = rowDataSnapshot;
    }


    @Override
    public Long id()
    {
        return new Id(mRowDataSnapshot).value();
    }


    @Override
    public Optional<CharSequence> title()
    {
        return new TaskTitle(mRowDataSnapshot);
    }


    @Override
    public Optional<DateTime> due()
    {
        return new EffectiveDueDate(mRowDataSnapshot);
    }


    @Override
    public Color color()
    {
        return new EffectiveTaskColor(mRowDataSnapshot);
    }


    @Override
    public Optional<Integer> percentComplete()
    {
        return new PercentComplete(mRowDataSnapshot);
    }
}
