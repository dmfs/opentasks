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

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.adapters.IntegerFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutDescriptor;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
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

	/**
	 * A {@link FieldAdapter} that knows how to load the color of the task list.
	 */
	private final static IntegerFieldAdapter LIST_COLOR_ADAPTER = new IntegerFieldAdapter(Tasks.LIST_COLOR);

	/**
	 * A {@link FieldAdapter} that knows how to load the color of a task.
	 */
	private final static IntegerFieldAdapter TASK_COLOR_ADAPTER = new IntegerFieldAdapter(Tasks.TASK_COLOR);

	/**
	 * The {@link ContentSet} that contains the value for this widget.
	 */
	protected ContentSet mValues;

	/**
	 * The {@link FieldDescriptor} that describes the field we show.
	 */
	protected FieldDescriptor mFieldDescriptor;

	/**
	 * The {@link LayoutOptions} for this widget.
	 */
	protected LayoutOptions mLayoutOptions;

	private int mFieldId = 0;


	public AbstractFieldView(Context context)
	{
		super(context);
	}


	public AbstractFieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		loadAttrs(attrs);
	}


	@SuppressLint("NewApi")
	public AbstractFieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		loadAttrs(attrs);
	}


	private void loadAttrs(AttributeSet attrs)
	{
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.AbstractFieldView);

		mFieldId = typedArray.getResourceId(R.styleable.AbstractFieldView_fieldDescriptor, 0);

		typedArray.recycle();
	}


	/**
	 * Returns the field id of this field or <code>0</code> if non was defined.
	 * 
	 * @return The field id of this field.
	 */
	public int getFieldId()
	{
		return mFieldId;
	}


	/**
	 * Set the {@link ContentSet} that contains the value for this widget.
	 * 
	 * @param values
	 *            A {@link ContentSet} containing the value.
	 */
	public void setValue(ContentSet values)
	{
		if (values == mValues)
		{
			// same values, nothing to do
			return;
		}

		FieldAdapter<?> adapter = mFieldDescriptor.getFieldAdapter();
		if (mValues != null)
		{
			// remove us from the old ContentSet if the ContentSet changes
			adapter.unregisterListener(mValues, this);
		}

		mValues = values;

		// set custom background color, if any
		Integer customBackgroud = getCustomBackgroundColor();
		if (customBackgroud != null)
		{
			setBackgroundColor(customBackgroud);
		}

		// register listener for updates
		if (values != null)
		{
			adapter.registerListener(values, this, true);
		}
	}


	/**
	 * Request the view to update the value {@link ContentSet} with all pending changes if any. This is usually called before a task is about to be saved.
	 */
	public void updateValues()
	{
		// nothing by default
	}


	/**
	 * Return a custom background color to set for this widget, can be <code>null</code> if this widget doesn't use a custom background color.
	 * 
	 * @return A custom color or <code>null</code>.
	 */
	public Integer getCustomBackgroundColor()
	{
		if (mValues != null)
		{
			if (mLayoutOptions.getBoolean(LayoutDescriptor.OPTION_USE_TASK_LIST_BACKGROUND_COLOR, false))
			{
				return LIST_COLOR_ADAPTER.get(mValues);
			}
			else if (mLayoutOptions.getBoolean(LayoutDescriptor.OPTION_USE_TASK_BACKGROUND_COLOR, false))
			{
				Integer taskColor = TASK_COLOR_ADAPTER.get(mValues);
				return taskColor == null ? LIST_COLOR_ADAPTER.get(mValues) : taskColor;
			}
		}
		return null;
	}


	/**
	 * Sets the {@link FieldDescriptor} for this widget.
	 * 
	 * @param descriptor
	 *            The {@link FieldDescriptor} that describes the field this widget shall show.
	 * @param options
	 *            Any {@link LayoutOptions}.
	 */
	@SuppressLint("DefaultLocale")
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions options)
	{
		mLayoutOptions = options;
		mFieldDescriptor = descriptor;
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
			}
		}

		// set icon if we have any

		// Note that the icon view is actually a TextView, not an ImageView and we just set a compound drawable. That ensures the image is always nicely
		// aligned with the first text line.
		TextView icon = (TextView) findViewById(android.R.id.icon);
		if (icon != null)
		{
			if (descriptor.getIcon() != 0)
			{
				icon.setCompoundDrawablesWithIntrinsicBounds(descriptor.getIcon(), 0, 0, 0);
				icon.setVisibility(View.VISIBLE);
			}
			else
			{
				icon.setVisibility(View.GONE);
			}
		}

	}


	/**
	 * Make up a text color for a given background color.
	 * <p>
	 * This method determines an approximate luminance of the background color and returns white for dark colors and a dark gray for bright colors.
	 * </p>
	 * 
	 * @param color
	 *            The background color.
	 * @return An appropriate text color.
	 */
	public static int getTextColorFromBackground(int color)
	{
		int redComponent = Color.red(color);
		int greenComponent = Color.green(color);
		int blueComponent = Color.blue(color);
		int alphaComponent = Color.alpha(color);
		int determinant = ((redComponent + redComponent + redComponent + blueComponent + greenComponent + greenComponent + greenComponent + greenComponent) >> 3)
			* alphaComponent / 255;
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
