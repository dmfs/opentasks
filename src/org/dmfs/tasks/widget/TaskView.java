/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
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

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Detail view of a task.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TaskView extends BaseTaskView
{

	private final SparseIntArray mAddedFields = new SparseIntArray(20);


	public TaskView(Context context)
	{
		super(context);
	}


	public TaskView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public TaskView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	/**
	 * Set the {@link Model} to use when showing the detail view.
	 * 
	 * @param model
	 *            The {@link Model}.
	 */
	public void setModel(Model model)
	{
		mAddedFields.clear();
		// first populate all views that are hardcoded in XML
		int childCount = getChildCount();
		for (int i = 0; i < childCount; ++i)
		{
			initChild(getChildAt(i), model);
		}

		final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		/*
		 * Add a detail view for every field that is supported by this model.
		 */
		for (FieldDescriptor field : model.getFields())
		{
			if (mAddedFields.get(field.getFieldId(), -1) == -1 && field.autoAdd())
			{
				AbstractFieldView detailView = field.getDetailView(inflater, this);
				if (detailView != null)
				{
					addView(detailView);
				}
				mAddedFields.put(field.getFieldId(), 1);
			}
		}
	}


	private void initChild(View child, Model model)
	{
		if (child instanceof AbstractFieldView)
		{
			int fieldId = ((AbstractFieldView) child).getFieldId();
			if (fieldId != 0)
			{
				FieldDescriptor fieldDescriptor = model.getField(fieldId);
				if (fieldDescriptor != null)
				{
					((AbstractFieldView) child).setFieldDescription(fieldDescriptor, fieldDescriptor.getViewLayoutOptions());
				}
				// remember that we added this field
				mAddedFields.put(fieldId, 1);
			}
		}

		if (child instanceof ViewGroup)
		{
			int childCount = ((ViewGroup) child).getChildCount();
			for (int i = 0; i < childCount; ++i)
			{
				initChild(((ViewGroup) child).getChildAt(i), model);
			}
		}
	}
}
