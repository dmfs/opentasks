/*
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

import org.dmfs.tasks.model.ContentSet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;


/**
 * Base view for task detail and editor views.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class BaseTaskView extends LinearLayout
{

	public BaseTaskView(Context context)
	{
		super(context);
	}


	public BaseTaskView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public BaseTaskView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	/**
	 * Set the {@link ContentSet} containing the values for the widgets in this view.
	 * 
	 * @param values
	 *            The values to insert into the widgets.
	 */
	public void setValues(ContentSet values)
	{
		int children = this.getChildCount();
		for (int i = 0; i < children; ++i)
		{
			View child = getChildAt(i);
			if (child instanceof AbstractFieldView)
			{
				((AbstractFieldView) child).setValue(values);
			}
		}
	}
}
