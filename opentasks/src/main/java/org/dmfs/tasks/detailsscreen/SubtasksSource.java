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

package org.dmfs.tasks.detailsscreen;

import android.content.Context;
import android.net.Uri;

import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.references.RowUriReference;
import org.dmfs.opentaskspal.rowsets.Subtasks;
import org.dmfs.opentaskspal.views.TasksView;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.readdata.CpQuerySource;
import org.dmfs.tasks.utils.rxjava.DelegatingSingle;
import org.dmfs.tasks.utils.rxjava.Offloading;

import io.reactivex.Single;

import static org.dmfs.provider.tasks.AuthorityUtil.taskAuthority;


/**
 * {@link Single} to get the subtasks of a task.
 *
 * @author Gabor Keszthelyi
 */
public final class SubtasksSource extends DelegatingSingle<Iterable<RowDataSnapshot<TaskContract.Tasks>>>
{
    public SubtasksSource(Context context, Uri taskUri, Projection<Tasks> projection)
    {
        super(new Offloading<>(
                        new CpQuerySource<>(
                                context.getApplicationContext(),
                                (client, ctx) -> new Subtasks(new TasksView(taskAuthority(context), client), projection, new RowUriReference<>(taskUri)))
                )
        );
    }
}
