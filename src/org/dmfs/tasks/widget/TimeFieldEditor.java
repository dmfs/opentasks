/*
 * TimeFieldEditor.java
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

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;

import android.content.Context;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;


/**
 * Widget to edit DateTime values
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * 
 */

public class TimeFieldEditor extends AbstractFieldEditor
{
	private static final String TAG = "TimeFieldEditor";
	TimeFieldAdapter mAdapter;
	EditText mText;


	public TimeFieldEditor(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	public TimeFieldEditor(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public TimeFieldEditor(Context context)
	{
		super(context);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mText = (EditText) findViewById(R.id.text);
	}


	@Override
	public void setup(FieldDescriptor descriptor)
	{
		super.setup(descriptor);
		mAdapter = (TimeFieldAdapter) descriptor.getFieldAdapter();
		mText.setHint(descriptor.getHint());
	}


	@Override
	protected void updateView()
	{
		Log.d("TimeFieldEditor", "CALLED");
		if (mValues != null && mAdapter.get(mValues) != null)
		{
			Log.d(TAG, "mValues is not null");
			Time dateTime = mAdapter.get(mValues);
			Log.d(TAG, Long.toString(dateTime.toMillis(true)));
			String formattedTime = dateTime.format("%d/%m/%Y %H:%M:%S");
			mText.setText(formattedTime);

		}

	}

}
