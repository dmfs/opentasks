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

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.StringFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;


/**
 * View widget for checklists and strings. The checklist mode is enabled automatically if any lines starts with <code>[X]</code> or <code>[ ]</code>.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class CheckListFieldView extends AbstractFieldEditor implements OnCheckedChangeListener
{
	private StringFieldAdapter mAdapter;
	private ViewGroup mContainer;
	private TextView mText;

	private String mCurrentValue;

	private boolean mBuilding = false;
	private LayoutInflater mInflater;


	public CheckListFieldView(Context context)
	{
		super(context);
		mInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	public CheckListFieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	public CheckListFieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		mInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mContainer = (ViewGroup) findViewById(R.id.checklist);
		mText = (TextView) findViewById(R.id.text);
	}


	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (StringFieldAdapter) descriptor.getFieldAdapter();
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (!mBuilding && mValues != null)
		{
			final String newText = buildDescription();
			final String oldText = mAdapter.get(mValues);
			if (!TextUtils.equals(newText, oldText)) // don't trigger unnecessary updates
			{
				mAdapter.set(mValues, newText);
			}
		}
	}


	@Override
	public void onContentLoaded(ContentSet contentSet)
	{
		super.onContentLoaded(contentSet);

		Integer customBackgroud = getCustomBackgroundColor();
		if (customBackgroud != null)
		{
			mText.setTextColor(getTextColorFromBackground(customBackgroud));
		}
	}


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		if (mValues != null)
		{
			String newValue = mAdapter.get(mValues);
			if (!TextUtils.equals(mCurrentValue, newValue)) // don't trigger unnecessary updates
			{
				updateCheckList(newValue);
				mCurrentValue = newValue;
				setVisibility(TextUtils.isEmpty(newValue) ? GONE : VISIBLE);
			}
		}
	}


	private String buildDescription()
	{
		StringBuilder builder = new StringBuilder(4 * 1024);
		String descriptionText = mText.getText().toString();
		builder.append(descriptionText);

		int count = mContainer.getChildCount();

		boolean first = descriptionText.length() == 0;
		for (int i = 0; i < count; ++i)
		{
			CheckBox checkbox = (CheckBox) mContainer.getChildAt(i);
			String text = checkbox.getText().toString();
			if (text.length() > 0)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					builder.append("\n");
				}

				builder.append(checkbox.isChecked() ? "[x] " : "[ ] ");
				builder.append(text);
			}
		}
		return builder.toString();
	}


	private void updateCheckList(String text)
	{
		Integer customBackgroud = getCustomBackgroundColor();
		if (customBackgroud != null)
		{
			mText.setTextColor(getTextColorFromBackground(customBackgroud));
		}

		if (text == null)
		{
			mContainer.setVisibility(GONE);
			return;
		}
		mContainer.setVisibility(VISIBLE);

		mBuilding = true;

		String[] items;
		if (text != null && text.length() > 0)
		{
			items = text.split("\n");
		}
		else
		{
			items = new String[0];
		}

		int count = 0;
		boolean inCheckListMode = false;
		int checkListStart = 0;

		for (int i = 0; i < items.length; ++i)
		{
			String item = items[i];
			boolean checked = false;
			if (item.startsWith("[x]") || item.startsWith("[X]"))
			{
				checked = true;
				item = item.substring(3).trim();
				inCheckListMode = true;
			}
			else if (item.startsWith("[ ]"))
			{
				item = item.substring(3).trim();
				inCheckListMode = true;
			}
			else if (!inCheckListMode)
			{
				checkListStart += item.length();
				if (i < items.length - 1)
				{
					++checkListStart;
				}
				continue;
			}

			if (item.length() == 0)
			{
				continue;
			}

			CheckBox checkbox = (CheckBox) mContainer.getChildAt(count);
			if (checkbox == null)
			{
				checkbox = (CheckBox) mInflater.inflate(R.layout.checklist_field_view_element, mContainer, false);
				checkbox.setOnCheckedChangeListener(CheckListFieldView.this);
				mContainer.addView(checkbox);
			}
			checkbox.setChecked(checked);
			checkbox.setText(item);

			++count;
		}

		while (mContainer.getChildCount() > count)
		{
			mContainer.removeViewAt(count);
		}

		mText.setText(text != null ? text.substring(0, checkListStart).trim() : null);
		mText.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);

		mBuilding = false;
	}
}
