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

package org.dmfs.opentaskspal.readdata;

import android.support.annotation.NonNull;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.android.bolts.color.colors.DelegatingColor;
import org.dmfs.android.bolts.color.elementary.SingleColor;
import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.projections.MultiProjection;
import org.dmfs.opentaskspal.readdata.functions.ColorFunction;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Tasks;


/**
 * {@link Color} for a task. Uses {@link Tasks#TASK_COLOR} if available,
 * fall backs to the mandatory {@link Tasks#LIST_COLOR} otherwise.
 *
 * @author Gabor Keszthelyi
 */
public final class EffectiveTaskColor extends DelegatingColor
{
    public static final Projection<Tasks> PROJECTION = new MultiProjection<>(Tasks.TASK_COLOR, Tasks.LIST_COLOR);


    public EffectiveTaskColor(@NonNull RowDataSnapshot<TaskContract.Tasks> rowData)
    {
        super(new SingleColor(
                new OptionalFallbackSingle<>(
                        new OptionalRowCharData<>(rowData, Tasks.TASK_COLOR, new ColorFunction()),
                        new RowCharData<>(rowData, Tasks.LIST_COLOR, new ColorFunction())
                )));
    }

}
