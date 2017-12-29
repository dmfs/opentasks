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

package org.dmfs.provider.tasks.model;

import android.content.ContentUris;
import android.net.Uri;

import org.dmfs.tasks.contract.TaskContract;


/**
 * An abstract implementation of a {@link InstanceAdapter} to server as the base for more concrete adapters.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class AbstractInstanceAdapter implements InstanceAdapter
{
    @Override
    public final Uri uri(String authority)
    {
        return ContentUris.withAppendedId(TaskContract.Tasks.getContentUri(authority), id());
    }
}