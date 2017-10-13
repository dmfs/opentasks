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

package org.dmfs.opentaskspal.views;

import android.content.ContentProviderClient;

import org.dmfs.android.contentpal.View;
import org.dmfs.android.contentpal.views.BaseView;
import org.dmfs.android.contentpal.views.DelegatingView;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link View} for the {@link TaskContract.Tasks} table.
 *
 * @author Gabor Keszthelyi
 */
public final class TasksView extends DelegatingView<TaskContract.Tasks>
{
    public TasksView(String authority, ContentProviderClient client)
    {
        super(new BaseView<TaskContract.Tasks>(client, TaskContract.Tasks.getContentUri(authority)));
    }
}
