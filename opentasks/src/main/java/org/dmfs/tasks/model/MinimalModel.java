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

package org.dmfs.tasks.model;

import android.content.Context;
import android.text.util.Linkify;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.layout.LayoutDescriptor;


/**
 * A minimal model every sync adapter should support.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class MinimalModel extends Model
{
    private final static LayoutDescriptor TEXT_VIEW = new LayoutDescriptor(R.layout.text_field_view).setOption(LayoutDescriptor.OPTION_LINKIFY, Linkify.ALL);
    private final static LayoutDescriptor TEXT_EDIT_SINGLE_LINE = new LayoutDescriptor(R.layout.text_field_editor).setOption(LayoutDescriptor.OPTION_MULTILINE,
            false);
    private final static LayoutDescriptor TIME_VIEW_ADD_BUTTON = new LayoutDescriptor(R.layout.time_field_view).setOption(
            LayoutDescriptor.OPTION_TIME_FIELD_SHOW_ADD_BUTTONS, true);
    private final static LayoutDescriptor TIME_EDIT = new LayoutDescriptor(R.layout.time_field_editor);


    MinimalModel(Context context, String accountType)
    {
        super(context, accountType);
    }


    @Override
    public void inflate()
    {
        if (mInflated)
        {
            return;
        }

        Context context = getContext();

        // task title
        addField(new FieldDescriptor(context, R.id.task_field_title, R.string.task_title, TaskFieldAdapters.TITLE).setViewLayout(TEXT_VIEW).setEditorLayout(
                TEXT_EDIT_SINGLE_LINE));

        // due
        addField(new FieldDescriptor(context, R.id.task_field_due, R.string.task_due, TaskFieldAdapters.DUE).setViewLayout(TIME_VIEW_ADD_BUTTON)
                .setEditorLayout(TIME_EDIT).setIcon(R.drawable.ic_detail_due));

        setAllowRecurrence(false);
        setAllowExceptions(false);

        mInflated = true;
    }
}
