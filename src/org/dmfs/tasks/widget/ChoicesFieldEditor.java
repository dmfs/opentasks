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

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.IChoicesAdapter;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
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

public class ChoicesFieldEditor extends AbstractFieldEditor
{
	private static final String TAG = "ChoicesFieldEditor";
	private FieldAdapter<Object> mAdapter;
	private Spinner mSpinner;

	private int mSelectedItem = ListView.INVALID_POSITION;
	private ChoicesSpinnerAdapter mSpinnerAdapter;


	public ChoicesFieldEditor(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	public ChoicesFieldEditor(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public ChoicesFieldEditor(Context context)
	{
		super(context);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mSpinner = (Spinner) findViewById(R.id.integer_choices_spinner);
		if (mSpinner == null)
		{
			return;
		}
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if (mSelectedItem != position && mValues != null)
				{
					Log.v(TAG, "onItemSelected" + position);
					mAdapter.set(mValues, mSpinnerAdapter.getItem(position));
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


	@SuppressWarnings("unchecked")
	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (FieldAdapter<Object>) descriptor.getFieldAdapter();

		IChoicesAdapter choicesAdapter = fieldDescriptor.getChoices();
		mSpinnerAdapter = new ChoicesSpinnerAdapter(getContext(), choicesAdapter);
		mSpinner.setAdapter(mSpinnerAdapter);
	}

	private class ChoicesSpinnerAdapter extends BaseAdapter implements SpinnerAdapter
	{
		LayoutInflater layoutInflater;
		IChoicesAdapter adapter;


		public ChoicesSpinnerAdapter(Context context, IChoicesAdapter a)
		{
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			adapter = a;
		}


		private void populateView(int position, View view)
		{
			SpinnerItemTag tag = (SpinnerItemTag) view.getTag();

			String title = adapter.getTitle(getItem(position));

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
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = layoutInflater.inflate(R.layout.integer_choices_spinner_selected_item, null);
				SpinnerItemTag tag = new SpinnerItemTag();
				tag.iv = (ImageView) convertView.findViewById(R.id.integer_choice_item_image);
				tag.tv = (TextView) convertView.findViewById(R.id.integer_choice_item_text);
				convertView.setTag(tag);
			}

			populateView(position, convertView);

			return convertView;
		}


		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = layoutInflater.inflate(R.layout.integer_choices_spinner_item, null);
				SpinnerItemTag tag = new SpinnerItemTag();
				tag.iv = (ImageView) convertView.findViewById(R.id.integer_choice_item_image);
				tag.tv = (TextView) convertView.findViewById(R.id.integer_choice_item_text);
				convertView.setTag(tag);
			}

			populateView(position, convertView);

			return convertView;
		}


		public int getPosition(Object object)
		{
			return adapter.getIndex(object);
		}


		@Override
		public int getCount()
		{
			return adapter.getCount();
		}


		@Override
		public Object getItem(int position)
		{
			return adapter.getItem(position);
		}


		@Override
		public long getItemId(int position)
		{
			return position;
		}


		public boolean hasTitle(Object object)
		{
			return adapter.getTitle(object) != null;
		}

		private class SpinnerItemTag
		{
			ImageView iv;
			TextView tv;
		}

	}


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		if (mValues != null)
		{
			if (mSpinnerAdapter != null)
			{
				Object mAdapterValue = mAdapter.get(mValues);

				int pos = mSpinnerAdapter.getPosition(mAdapterValue);

				if (!mSpinnerAdapter.hasTitle(mAdapterValue))
				{
					// hide spinner if the current element has no title or there is no current element
					setVisibility(View.GONE);
					return;
				}

				setVisibility(View.VISIBLE);

				if (pos != mSelectedItem)
				{
					mSelectedItem = pos;
					mSpinner.setSelection(mSelectedItem);
				}
				else
				{
					// something else must have changed, better invalidate the list
					mSpinnerAdapter.notifyDataSetChanged();
				}
			}
		}
	}
}
