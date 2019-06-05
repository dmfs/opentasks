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

package org.dmfs.tasks.share;

import androidx.annotation.Nullable;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.TaskFieldAdapters;

import au.com.codeka.carrot.Bindings;


/**
 * {@link Bindings} for accessing values from a task.
 * See the <code>switch-case</code> for the supported properties.
 *
 * @author Gabor Keszthelyi
 */
public final class TaskBindings implements Bindings
{
    private final ContentSet mContentSet;
    private final Model mModel;


    public TaskBindings(ContentSet contentSet, Model model)
    {
        mContentSet = contentSet;
        mModel = model;
    }


    @Nullable
    @Override
    public Object resolve(String key)
    {
        switch (key)
        {
            case "title":
                return TaskFieldAdapters.TITLE.get(mContentSet);

            case "description":
                return TaskFieldAdapters.DESCRIPTION.get(mContentSet);

            case "checklist":
                return TaskFieldAdapters.CHECKLIST.get(mContentSet);

            case "location":
                return TaskFieldAdapters.LOCATION.get(mContentSet);

            case "start":
                return TaskFieldAdapters.DTSTART.get(mContentSet);

            case "due":
                return TaskFieldAdapters.DUE.get(mContentSet);

            case "completed":
                return TaskFieldAdapters.COMPLETED.get(mContentSet);

            case "priority":
                Integer priority = TaskFieldAdapters.PRIORITY.get(mContentSet);
                return priority == null ? null : mModel.getField(R.id.task_field_priority).getChoices().getTitle(priority);

            case "privacy":
                Integer classification = TaskFieldAdapters.CLASSIFICATION.get(mContentSet);
                return classification == null ? null : mModel.getField(R.id.task_field_classification).getChoices().getTitle(classification);

            case "status":
                Integer status = TaskFieldAdapters.STATUS.get(mContentSet);
                return status == null ? null : mModel.getField(R.id.task_field_status).getChoices().getTitle(status);

            case "url":
                return TaskFieldAdapters.URL.get(mContentSet);

            default:
                return null;
        }
    }


    @Override
    public boolean isEmpty()
    {
        return false;
    }

}
