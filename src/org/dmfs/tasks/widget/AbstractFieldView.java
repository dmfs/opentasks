/*
 * AbstractFieldEditor.java
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

import android.content.ContentValues;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Mother of all field views and editors.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class AbstractFieldView extends LinearLayout
{

	private final static String TAG = "AbstractFieldView";
	protected ContentValues mValues;
	private TextView mTitleId;
	protected FieldDescriptor fieldDescriptor;

	public AbstractFieldView(Context context)
	{
		super(context);
	}


	public AbstractFieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public AbstractFieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		try
		{
			mTitleId = (TextView) findViewById(android.R.id.title);
		}
		catch (Throwable e)
		{
			// no title
			Log.i(TAG, "can't find title id ", e);
			mTitleId = null;
		}
	}


	public void setValue(ContentValues values)
	{
		mValues = values;
		updateView();
	}


	public void setup(FieldDescriptor descriptor)
	{
		fieldDescriptor = descriptor;
		if (mTitleId != null)
		{
			mTitleId.setText(descriptor.getTitle());
		}
	}


	protected abstract void updateView();
}
