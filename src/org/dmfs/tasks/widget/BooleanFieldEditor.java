/*
 * 
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

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.BooleanFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * Editor for boolean values.
 * @author Arjun Naik<arjun@arjunnaik.in>
 *
 */
public class BooleanFieldEditor extends AbstractFieldEditor
{
	CheckBox mCheckBox;
	BooleanFieldAdapter mAdapter;

	public BooleanFieldEditor(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}


	public BooleanFieldEditor(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}


	public BooleanFieldEditor(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mCheckBox = (CheckBox) findViewById(android.R.id.text1);
	}


	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (BooleanFieldAdapter) descriptor.getFieldAdapter();
		mCheckBox.setHint(descriptor.getHint());
	}

	@Override
	public void onContentChanged(ContentSet contentSet, String key)
	{
		Boolean newValue = mAdapter.get(mValues);
		if (mValues != null && newValue != null)
		{
			mCheckBox.setChecked(newValue.booleanValue());
		}
	}
	
}
