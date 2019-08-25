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

import androidx.annotation.NonNull;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.android.bolts.color.elementary.DelegatingColor;
import org.dmfs.android.bolts.color.elementary.SingleColor;
import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.projections.MultiProjection;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.opentaskspal.readdata.functions.StringToColor;
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
    public static final Projection<? super TaskContract.TaskColumns> PROJECTION = new MultiProjection<>(Tasks.TASK_COLOR, Tasks.LIST_COLOR);


    public EffectiveTaskColor(@NonNull RowDataSnapshot<? extends TaskContract.TaskColumns> rowData)
    {
        super(new SingleColor(
                new Backed<Color>(
                        rowData.data(Tasks.TASK_COLOR, StringToColor.FUNCTION),
                        () -> rowData.data(Tasks.LIST_COLOR, StringToColor.FUNCTION).value()
                )));
    }

}
