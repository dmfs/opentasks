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
import android.content.Intent;
import android.net.Uri;

import org.dmfs.tasks.utils.Showable;


/**
 * UI for showing the details of a task.
 * <p>
 * On {@link #show(Context)} it starts an Activity with action VIEW and with the task Uri as data.
 *
 * @author Gabor Keszthelyi
 */
public final class TaskDetailsUi implements Showable<Context>
{
    private final Uri mTaskUri;


    public TaskDetailsUi(Uri taskUri)
    {
        mTaskUri = taskUri;
    }


    @Override
    public void show(Context context)
    {
        context.startActivity(new Intent(Intent.ACTION_VIEW, mTaskUri));
    }
}
