/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.utils;

import java.util.ArrayList;

import org.dmfs.android.widgets.ColoredShapeCheckBox;
import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.tasks.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * An adapter to adapt a cursor containing task lists to a {@link Spinner}.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TasksListCursorAdapter extends android.support.v4.widget.CursorAdapter
{
	LayoutInflater mInflater;

	private int mTaskColorColumn;
	private int mTaskNameColumn;
	private int mAccountNameColumn;
	private int mIdColumn;
	private ArrayList<Long> mSelectedLists = new ArrayList<Long>(20);

	private SelectionEnabledListener mListener;


	public TasksListCursorAdapter(Context context)
	{
		super(context, null, 0 /* don't register a content observer to avoid a context leak! */);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	public void setSelectionEnabledListener(SelectionEnabledListener listener)
	{
		mListener = listener;
	}


	@Override
	public Cursor swapCursor(Cursor c)
	{
		Cursor result = super.swapCursor(c);
		if (c != null)
		{
			mIdColumn = c.getColumnIndex(TaskContract.TaskListColumns._ID);
			mTaskColorColumn = c.getColumnIndex(TaskContract.TaskListColumns.LIST_COLOR);
			mTaskNameColumn = c.getColumnIndex(TaskContract.TaskListColumns.LIST_NAME);
			mAccountNameColumn = c.getColumnIndex(TaskContract.TaskListSyncColumns.ACCOUNT_NAME);

			c.moveToPosition(-1);
			mSelectedLists = new ArrayList<Long>(c.getCount());
			while (c.moveToNext())
			{
				mSelectedLists.add(c.getLong(mIdColumn));
			}
		}
		return result;
	}


	@Override
	public void bindView(View v, Context context, Cursor c)
	{
		/* Since we override getView and get DropDownView we don't need this method. */
	}


	@Override
	public View newView(Context context, Cursor c, ViewGroup vg)
	{
		/* Since we override getView and get DropDownView we don't need this method. */
		return null;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{

		Cursor cursor = (Cursor) getItem(position);
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.list_item_selection, null);
		}

		TextView tvListName = (TextView) convertView.findViewById(android.R.id.text1);
		TextView tvAccountName = (TextView) convertView.findViewById(android.R.id.text2);
		final ColoredShapeCheckBox cBox = (ColoredShapeCheckBox) convertView.findViewById(android.R.id.checkbox);

		final String listName = cursor.getString(mTaskNameColumn);
		final String accountName = cursor.getString(mAccountNameColumn);
		final Long id = cursor.getLong(mIdColumn);

		tvListName.setText(listName);
		tvAccountName.setText(accountName);
		int taskListColor = cursor.getInt(mTaskColorColumn);
		cBox.setColor(taskListColor);
		cBox.setChecked(mSelectedLists.contains(id));

		// listen for checkbox
		convertView.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View view)
			{
				boolean isChecked = !cBox.isChecked();
				cBox.setChecked(isChecked);
				int oldSize = mSelectedLists.size();

				if (isChecked)
				{
					if (!mSelectedLists.contains(id))
					{
						mSelectedLists.add(id);
					}

				}
				else
				{
					mSelectedLists.remove(id);
				}

				if (mListener != null)
				{
					if (oldSize == 0 && mSelectedLists.size() > 0)
					{
						mListener.onSelectionEnabled();
					}
					if (oldSize > 0 && mSelectedLists.size() == 0)
					{
						mListener.onSelectionDisabled();
					}
				}
			}
		});

		return convertView;
	}


	public ArrayList<Long> getSelectedLists()
	{
		return mSelectedLists;
	}

	/**
	 * Listener that is used to notify if the select item count is > 0 or equal 0.
	 * 
	 * @author Tobias Reinsch <tobias@dmfs.org>
	 * 
	 */
	public interface SelectionEnabledListener
	{
		public void onSelectionEnabled();


		public void onSelectionDisabled();
	}
}
