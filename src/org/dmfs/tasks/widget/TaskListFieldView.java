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

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.ListDetails;
import org.dmfs.tasks.model.adapters.ListDetailsFieldAdapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;


public class TaskListFieldView extends AbstractFieldView
{
	private static final String TAG = "TaskListFieldView";
	TextView mTextListName;
	TextView mTextListAccount;
	ListDetailsFieldAdapter mAdapter;


	public TaskListFieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	public TaskListFieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public TaskListFieldView(Context context)
	{
		super(context);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mTextListName = (TextView) findViewById(R.id.task_list_name);
		mTextListAccount = (TextView) findViewById(R.id.task_list_account);
	}


	@Override
	public void setup(FieldDescriptor descriptor, Activity context)
	{
		super.setup(descriptor, context);
		mAdapter = (ListDetailsFieldAdapter) descriptor.getFieldAdapter();
		mTextListName.setHint(descriptor.getHint());
	}


	@Override
	protected void updateView()
	{
		Log.d(TAG, "mValues : " + mValues);
		Log.d(TAG, "Adapter Value : " + mAdapter.get(mValues));

		if (mValues != null && mAdapter.get(mValues) != null)
		{
			ListDetails dets = mAdapter.get(mValues);
			mTextListName.setText(dets.listName);
			mTextListAccount.setText(dets.listAccountName);
			int selectedColor = getTextColorFromBackground(dets.listColor);
			setBackgroundColor(dets.listColor);
			mTextListAccount.setTextColor(selectedColor);
			mTextListName.setTextColor(selectedColor);
			Log.d(TAG, "List Color : " + dets.listColor);

		}
	}


	public static int getTextColorFromBackground(int color)
	{
		int redComponent = Color.red(color);
		int greenComponent = Color.green(color);
		int blueComponent = Color.blue(color);
		int alphaComponent = Color.alpha(color);
		Log.d(TAG, "Red Component : " + redComponent);
		int determinant = ((redComponent + redComponent + redComponent + blueComponent + greenComponent + greenComponent + greenComponent + greenComponent) >> 3) * alphaComponent / 255;
		Log.d(TAG, "Determinant : " + determinant);
		// Value 160 has been set by trial and error.
		if (determinant > 160)
		{
			return Color.BLACK;
		}
		else
		{
			return Color.WHITE;

		}
	}
}
