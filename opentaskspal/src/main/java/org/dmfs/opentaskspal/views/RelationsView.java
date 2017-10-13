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

import org.dmfs.android.contentpal.views.BaseView;
import org.dmfs.android.contentpal.views.DelegatingView;
import org.dmfs.opentaskspal.contentpal.views.Filtered;
import org.dmfs.opentaskspal.predicates.Relation;
import org.dmfs.tasks.contract.TaskContract;


/**
 * @author Gabor Keszthelyi
 */
public final class RelationsView extends DelegatingView<TaskContract.Properties>
{
    public RelationsView(String authority, ContentProviderClient client)
    {
        super(new Filtered<>(new Relation(), new BaseView<TaskContract.Properties>(client, TaskContract.Properties.getContentUri(authority))));
    }
}
