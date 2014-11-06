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

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.util.AttributeSet;


/**
 * A widget that shows the string representation of an object.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ListColorView extends AbstractFieldView
{
	/**
	 * The {@link FieldAdapter} of the field for this view.
	 */
	private FieldAdapter<Integer> mAdapter;


	public ListColorView(Context context)
	{
		super(context);
	}


	public ListColorView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public ListColorView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (FieldAdapter<Integer>) descriptor.getFieldAdapter();
	}


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		if (mValues != null)
		{
			this.setBackgroundColor(mAdapter.get(contentSet));
		}
	}
}
