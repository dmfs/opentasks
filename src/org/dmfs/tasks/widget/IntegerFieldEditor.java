/*
 * IntegerFieldEditor.java
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

import java.util.List;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ArrayChoicesAdapter;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.IChoicesAdapter;
import org.dmfs.tasks.model.adapters.IntegerFieldAdapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;


/**
 * Widget to edit Integer values having a {@link IChoicesAdapter}.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 * 
 */

public class IntegerFieldEditor extends AbstractFieldEditor
{
	private static final String TAG = "IntegerFieldEditor";
	private IntegerFieldAdapter mAdapter;
	private Spinner mSpinner;

	private int mSelectedItem = ListView.INVALID_POSITION;
	private IntegerSpinnerAdapter mSpinnerAdapter;


	public IntegerFieldEditor(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	public IntegerFieldEditor(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public IntegerFieldEditor(Context context)
	{
		super(context);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mSpinner = (Spinner) findViewById(R.id.integer_choices_spinner);
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if (mSelectedItem != position)
				{
					Log.v(TAG, "onItemSelected" + position);
					mAdapter.set(mValues, (Integer) ((ArrayChoicesAdapter) fieldDescriptor.getChoices()).getChoices().get(position));
					mSelectedItem = position;
				}
			}


			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				mSelectedItem = ListView.INVALID_POSITION;
				mAdapter.set(mValues, null);
			}
		});
	}


	@Override
	public void setup(FieldDescriptor descriptor, Activity context)
	{
		super.setup(descriptor, context);
		mAdapter = (IntegerFieldAdapter) descriptor.getFieldAdapter();

		IChoicesAdapter choicesAdapter = fieldDescriptor.getChoices();

		if (choicesAdapter instanceof ArrayChoicesAdapter)
		{
			ArrayChoicesAdapter arrayAdapter = (ArrayChoicesAdapter) choicesAdapter;
			List<Object> choicesList = arrayAdapter.getChoices();
			mSpinnerAdapter = new IntegerSpinnerAdapter(mContext, R.layout.integer_choices_spinner_item, R.id.integer_choice_item_text, choicesList,
				arrayAdapter);
			mSpinner.setAdapter(mSpinnerAdapter);
		}
	}

	private class IntegerSpinnerAdapter extends ArrayAdapter<Object> implements SpinnerAdapter
	{
		LayoutInflater layoutInflater;
		ArrayChoicesAdapter adapter;


		public IntegerSpinnerAdapter(Context context, int resource, int textViewResourceId, List<Object> objects, ArrayChoicesAdapter a)
		{
			super(context, resource, textViewResourceId, objects);
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			adapter = a;
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			SpinnerItemTag tag;
			if (convertView == null)
			{
				convertView = layoutInflater.inflate(R.layout.integer_choices_spinner_item, null);
				tag = new SpinnerItemTag();
				tag.iv = (ImageView) convertView.findViewById(R.id.integer_choice_item_image);
				tag.tv = (TextView) convertView.findViewById(R.id.integer_choice_item_text);
				convertView.setTag(tag);
			}
			else
			{
				tag = (SpinnerItemTag) convertView.getTag();
			}

			String title = adapter.getTitle(getItem(position));
			Log.d(TAG, Integer.toString(position) + " Title : " + title);

			Drawable image = adapter.getDrawable(getItem(position));

			if (image != null)
			{
				tag.iv.setImageDrawable(image);
			}
			else
			{
				tag.iv.setVisibility(View.GONE);
			}
			tag.tv.setText(title);

			return convertView;
		}


		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
			return getView(position, convertView, parent);
		}

		private class SpinnerItemTag
		{
			ImageView iv;
			TextView tv;
		}

	}


	@Override
	public void onContentChanged(ContentSet contentSet, String key)
	{
		if (mValues != null)
		{
			if (mSpinnerAdapter != null)
			{
				int pos = mSpinnerAdapter.getPosition(mAdapter.get(mValues));
				if (pos != mSelectedItem)
				{
					mSelectedItem = pos;
					mSpinner.setSelection(mSelectedItem);
				}
			}
		}
	}
}
