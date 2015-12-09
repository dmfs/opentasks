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

import java.util.List;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.CheckListItem;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.ChecklistFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;


/**
 * View widget for checklists.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class CheckListFieldViewLegacy extends AbstractFieldView implements OnCheckedChangeListener
{
	private ChecklistFieldAdapter mAdapter;
	private ViewGroup mContainer;

	private List<CheckListItem> mCurrentValue;

	private boolean mBuilding = false;
	private LayoutInflater mInflater;


	public CheckListFieldViewLegacy(Context context)
	{
		super(context);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	public CheckListFieldViewLegacy(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	public CheckListFieldViewLegacy(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mContainer = (ViewGroup) findViewById(R.id.checklist);
	}


	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (ChecklistFieldAdapter) descriptor.getFieldAdapter();
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (mCurrentValue == null || mBuilding)
		{
			return;
		}

		int childCount = mContainer.getChildCount();
		for (int i = 0; i < childCount; ++i)
		{
			if (mContainer.getChildAt(i) == buttonView)
			{
				mCurrentValue.get(i).checked = isChecked;
				buttonView.setTextAppearance(getContext(), isChecked ? R.style.checklist_checked_item_text : R.style.dark_text);
				if (mValues != null)
				{
					mAdapter.validateAndSet(mValues, mCurrentValue);
				}
				return;
			}
		}
	}


	@Override
	public void onContentLoaded(ContentSet contentSet)
	{
		super.onContentLoaded(contentSet);
	}


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		if (mValues != null)
		{
			List<CheckListItem> newValue = mAdapter.get(mValues);
			if (newValue != null && !newValue.equals(mCurrentValue)) // don't trigger unnecessary updates
			{
				updateCheckList(newValue);
				mCurrentValue = newValue;
			}
		}
	}


	private void updateCheckList(List<CheckListItem> list)
	{
		Context context = getContext();

		if (list == null || list.size() == 0)
		{
			setVisibility(GONE);
			return;
		}
		setVisibility(VISIBLE);

		mBuilding = true;

		int count = 0;
		for (CheckListItem item : list)
		{
			CheckBox checkbox = (CheckBox) mContainer.getChildAt(count);
			if (checkbox == null)
			{
				checkbox = (CheckBox) mInflater.inflate(R.layout.checklist_field_view_element, mContainer, false);
				mContainer.addView(checkbox);
			}
			// make sure we don't receive our own updates
			checkbox.setOnCheckedChangeListener(null);
			checkbox.setChecked(item.checked);
			checkbox.setOnCheckedChangeListener(CheckListFieldViewLegacy.this);

			checkbox.setTextAppearance(context, item.checked ? R.style.checklist_checked_item_text : R.style.dark_text);
			checkbox.setText(item.text);

			++count;
		}

		while (mContainer.getChildCount() > count)
		{
			mContainer.removeViewAt(count);
		}

		mBuilding = false;
	}
}
