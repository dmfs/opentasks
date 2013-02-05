/*
 * TaskView.java
 *
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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
 * 
 */

package org.dmfs.tasks.widget;

import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.Model;

import android.content.ContentValues;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;


public class TaskEdit extends BaseTaskView
{

	private ContentValues mValues;
	private Model mModel;


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


	@Override
	protected void onFinishInflate()
	{
	}


	public void setModel(Model model)
	{
		mModel = model;
	}


	public void setValues(ContentValues values)
	{
		mValues = values;
		updateView();

	}


	private void updateView()
	{
		final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (FieldDescriptor field : mModel.getFields())
		{

			AbstractFieldView editView = field.getEditorView(inflater);
			if (editView != null)
			{
				editView.setup(field, getActivity());
				editView.setValue(mValues);
				editView.updateView();
				this.addView(editView);
			}
		}
	}

}
