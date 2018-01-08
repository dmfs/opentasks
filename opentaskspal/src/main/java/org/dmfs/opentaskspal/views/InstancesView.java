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

package org.dmfs.opentaskspal.views;

import android.content.ContentProviderClient;

import org.dmfs.android.contentpal.View;
import org.dmfs.android.contentpal.views.BaseView;
import org.dmfs.android.contentpal.views.DelegatingView;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link View} for the {@link TaskContract.Instances} table.
 *
 * @author Marten Gajda
 */
public final class InstancesView<T extends TaskContract.Instances> extends DelegatingView<T>
{
    public InstancesView(String authority, ContentProviderClient client)
    {
        super(new BaseView<>(client, TaskContract.Instances.getContentUri(authority)));
    }
}
