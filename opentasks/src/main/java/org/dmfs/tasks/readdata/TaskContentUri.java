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

package org.dmfs.tasks.readdata;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;

import org.dmfs.jems.single.Single;
import org.dmfs.provider.tasks.AuthorityUtil;
import org.dmfs.tasks.contract.TaskContract;


/**
 * Content Uri for a given task id.
 *
 * @author Gabor Keszthelyi
 */
public final class TaskContentUri implements Single<Uri>
{
    private final Long mTaskId;
    private final Context mAppContext;


    public TaskContentUri(Long taskId, Context context)
    {
        mTaskId = taskId;
        mAppContext = context.getApplicationContext();
    }


    @Override
    public Uri value()
    {
        return ContentUris.withAppendedId(TaskContract.Tasks.getContentUri(AuthorityUtil.taskAuthority(mAppContext)), mTaskId);
    }
}
