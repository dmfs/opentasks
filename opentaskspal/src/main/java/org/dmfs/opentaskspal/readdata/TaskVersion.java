/*
 * Copyright 2019 dmfs GmbH
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

import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.projections.SingleColProjection;
import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.jems.single.decorators.DelegatingSingle;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Tasks;


/**
 * {@link Single} for the {@link Tasks#VERSION} value of a task.
 *
 * @author Marten Gajda
 */
public final class TaskVersion extends DelegatingSingle<Integer>
{
    public static final Projection<? super TaskContract.TaskColumns> PROJECTION = new SingleColProjection<>(Tasks.VERSION);


    public TaskVersion(@NonNull RowDataSnapshot<? extends TaskContract.TaskColumns> rowDataSnapshot)
    {
        super(new Backed<>(rowDataSnapshot.data(Tasks.VERSION, Integer::parseInt), 0));
    }
}
