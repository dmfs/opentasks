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

package org.dmfs.tasks.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.Model;


/**
 * Editor view for a task.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TaskEdit extends BaseTaskView
{

    public TaskEdit(Context context)
    {
        super(context);
    }


    public TaskEdit(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public TaskEdit(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


    /**
     * Set the {@link Model} to use when showing the detail view.
     *
     * @param model
     *         The {@link Model}.
     */
    public void setModel(Model model)
    {
        Model mModel = model;
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        /*
         * Add an editor for every field that is supported by this model.
         */
        for (FieldDescriptor field : mModel.getFields())
        {
            if (field.autoAdd())
            {
                AbstractFieldView detailView = field.getEditorView(inflater, this);
                if (detailView != null)
                {
                    addView(detailView);
                }
            }
        }
    }
}
