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

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.adapters.IntegerFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutDescriptor;
import org.dmfs.tasks.model.layout.LayoutOptions;

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
public abstract class AbstractFieldView extends LinearLayout implements OnContentChangeListener
{

	private final static String TAG = "AbstractFieldView";

	private final static IntegerFieldAdapter LIST_COLOR_ADAPTER = new IntegerFieldAdapter(Tasks.LIST_COLOR);
	private final static IntegerFieldAdapter TASK_COLOR_ADAPTER = new IntegerFieldAdapter(Tasks.TASK_COLOR);

	protected ContentSet mValues;
	protected FieldDescriptor fieldDescriptor;

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


	public void setValue(ContentSet values)
	{
		FieldAdapter<?> adapter = fieldDescriptor.getFieldAdapter();
		if (mValues != null)
		{
			// remove us if the ContentSet changes
			adapter.unregisterListener(mValues, this);
		}

		mValues = values;
		Integer customBackgroud = getCustomBackgroundColor();
		if (customBackgroud != null)
		{
			setBackgroundColor(customBackgroud);
		}
		if (values != null)
		{
			adapter.registerListener(values, this, true);
		}
	}


	public Integer getCustomBackgroundColor()
	{
		if (mValues != null)
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
		}
		return null;
	}


	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions options)
	{
		layoutOptions = options;
		fieldDescriptor = descriptor;
		TextView titleId = (TextView) findViewById(android.R.id.title);
		if (titleId != null)
		{
			if (options.getBoolean(LayoutDescriptor.OPTION_NO_TITLE, false))
			{
				titleId.setVisibility(View.GONE);
			}
			else
			{
				titleId.setText(descriptor.getTitle().toUpperCase());
				Integer customBackgroud = getCustomBackgroundColor();
				if (customBackgroud != null)
				{
					titleId.setTextColor(AbstractFieldView.getTextColorFromBackground(customBackgroud));
				}
			}
		}
	}


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
		// Value 180 has been set by trial and error.
		if (determinant > 180)
		{
			return Color.argb(255, 0x33, 0x33, 0x33);
		}
		else
		{
			return Color.WHITE;
		}
	}


	@Override
	public void onContentLoaded(ContentSet contentSet)
	{
		// handle reloaded content sets just like updated content sets
		onContentChanged(contentSet);
	}
}
