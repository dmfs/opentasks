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

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.IntegerFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutDescriptor;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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

	private final static IntegerFieldAdapter LIST_COLOR_ADAPTER = new IntegerFieldAdapter(Tasks.LIST_COLOR);
	private final static IntegerFieldAdapter TASK_COLOR_ADAPTER = new IntegerFieldAdapter(Tasks.TASK_COLOR);

	protected ContentValues mValues;
	private TextView mTitleId;
	protected FieldDescriptor fieldDescriptor;
	protected Activity mContext;

	protected LayoutOptions layoutOptions;


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


	public void setOptions(LayoutOptions options)
	{
		this.layoutOptions = options;
	}


	public void setValue(ContentValues values)
	{
		mValues = values;
		Integer customBackgroud = getCustomBackgroudColor();
		if (customBackgroud != null)
		{
			setBackgroundColor(customBackgroud);
		}
		updateView();

	}


	public Integer getCustomBackgroudColor()
	{
		if (layoutOptions.getBoolean(LayoutDescriptor.OPTION_USE_TASK_LIST_BACKGROUND_COLOR, false))
		{
			return LIST_COLOR_ADAPTER.get(mValues);
		}
		else if (layoutOptions.getBoolean(LayoutDescriptor.OPTION_USE_TASK_BACKGROUND_COLOR, false))
		{
			Integer taskColor = TASK_COLOR_ADAPTER.get(mValues);
			return taskColor == null ? LIST_COLOR_ADAPTER.get(mValues) : taskColor;
		}
		return null;
	}


	public void setup(FieldDescriptor descriptor, Activity context)
	{
		mContext = context;
		fieldDescriptor = descriptor;
		if (mTitleId != null)
		{
			if (layoutOptions.getBoolean(LayoutDescriptor.OPTION_NO_TITLE, false))
			{
				mTitleId.setVisibility(View.GONE);
			}
			else
			{
				mTitleId.setText(descriptor.getTitle().toUpperCase());
				Integer customBackgroud = getCustomBackgroudColor();
				if (customBackgroud != null)
				{
					mTitleId.setTextColor(AbstractFieldView.getTextColorFromBackground(customBackgroud));
				}
			}
		}
	}


	protected abstract void updateView();


	public static int getTextColorFromBackground(int color)
	{
		int redComponent = Color.red(color);
		int greenComponent = Color.green(color);
		int blueComponent = Color.blue(color);
		int alphaComponent = Color.alpha(color);
		Log.d(TAG, "Red Component : " + redComponent);
		int determinant = ((redComponent + redComponent + redComponent + blueComponent + greenComponent + greenComponent + greenComponent + greenComponent) >> 3)
			* alphaComponent / 255;
		Log.d(TAG, "Determinant : " + determinant);
		// Value 190 has been set by trial and error.
		if (determinant > 180)
		{
			return Color.argb(255, 0x33, 0x33, 0x33);
		}
		else
		{
			return Color.WHITE;

		}
	}
}
