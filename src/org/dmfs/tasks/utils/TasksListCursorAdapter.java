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
package org.dmfs.tasks.utils;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.tasks.R;
import org.dmfs.tasks.widget.AbstractFieldView;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;


public class TasksListCursorAdapter extends android.support.v4.widget.CursorAdapter implements SpinnerAdapter
{
	LayoutInflater mInflater;

	private int taskColorColumn;
	private int taskNameColumn;
	private int accountNameColumn;


	public TasksListCursorAdapter(Context context)
	{
		super(context, null, false);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public void changeCursor(Cursor c)
	{
		super.changeCursor(c);
		if (c != null)
		{
			taskColorColumn = c.getColumnIndex(TaskContract.TaskListColumns.LIST_COLOR);
			taskNameColumn = c.getColumnIndex(TaskContract.TaskListColumns.LIST_NAME);
			accountNameColumn = c.getColumnIndex(TaskContract.TaskListSyncColumns.ACCOUNT_NAME);
		}
	}


	@Override
	public void bindView(View v, Context context, Cursor c)
	{

	}


	@Override
	public View newView(Context context, Cursor c, ViewGroup vg)
	{
		return null;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.list_spinner_item_selected, null);

		}
		TextView listName = (TextView) convertView.findViewById(R.id.task_list_name);
		TextView accountName = (TextView) convertView.findViewById(R.id.task_list_account_name);
		Cursor cursor = (Cursor) getItem(position);

		listName.setText(cursor.getString(taskNameColumn));
		accountName.setText(cursor.getString(accountNameColumn));
		int taskListColor = cursor.getInt(taskColorColumn);
		int backgroundBasedColor = AbstractFieldView.getTextColorFromBackground(taskListColor);

		listName.setTextColor(backgroundBasedColor);
		accountName.setTextColor(backgroundBasedColor);
		return convertView;
	}


	public View getDropDownView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.list_spinner_item_dropdown, null);

		}

		View listColor = convertView.findViewById(R.id.task_list_color);
		TextView listName = (TextView) convertView.findViewById(R.id.task_list_name);
		TextView accountName = (TextView) convertView.findViewById(R.id.task_list_account_name);
		Cursor cursor = (Cursor) getItem(position);

		listColor.setBackgroundColor(cursor.getInt(taskColorColumn));
		listName.setText(cursor.getString(taskNameColumn));
		accountName.setText(cursor.getString(accountNameColumn));
		return convertView;
	}
}
