/*
 * IntegerFieldView.java
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
import org.dmfs.tasks.model.IChoicesAdapter;
import org.dmfs.tasks.model.adapters.IntegerFieldAdapter;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;


/**
 * Widget to display Integer values.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * 
 */

public class IntegerFieldView extends AbstractFieldView
{

	private static final String TAG = "IntegerFieldView";
	private IntegerFieldAdapter mAdapter;
	private TextView mText;


	public IntegerFieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

	}


	public IntegerFieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

	}


	public IntegerFieldView(Context context)
	{
		super(context);

	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mText = (TextView) findViewById(R.id.text);
	}


	@Override
	public void setup(FieldDescriptor descriptor)
	{
		super.setup(descriptor);
		mAdapter = (IntegerFieldAdapter) descriptor.getFieldAdapter();
		mText.setHint(descriptor.getHint());
	}


	@Override
	protected void updateView()
	{
		Log.d(TAG, "mValues : " + mValues);
		Log.d(TAG, "Adapter Value : " + mAdapter.get(mValues));
		Log.d(TAG, "mText:" + mText);

		if (mValues != null && mAdapter.get(mValues) != null)
		{
			IChoicesAdapter choicesAdapter = fieldDescriptor.getChoices();
			Log.d(TAG, "ChoicesAdapter : " + choicesAdapter);
			mText.setText(choicesAdapter == null ? mAdapter.get(mValues).toString() : choicesAdapter.getTitle(mAdapter.get(mValues)));
		}
	}

}
