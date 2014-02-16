/*
 * 
 *
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

package org.dmfs.tasks.homescreen.utils;

import java.lang.reflect.Array;

import org.dmfs.tasks.model.TaskFieldAdapters;

import android.database.Cursor;


/**
 * The is used to generate an {@link Array} of {@link TaskListWidgetItem} by reading a {@link Cursor}.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 */
public class WidgetCusorListGenerator
{

	/** The tasks cursor. */
	private Cursor mTasksCursor;


	/**
	 * Instantiates a new widget cursor list generator.
	 * 
	 * @param cursor
	 *            the cursor with due tasks
	 */
	public WidgetCusorListGenerator(Cursor cursor)
	{
		mTasksCursor = cursor;
	}


	/**
	 * Gets the array of {@link TaskListWidgetItem}s.
	 * 
	 * @return the widget items
	 */
	public TaskListWidgetItem[] getWidgetItems()
	{
		if (mTasksCursor.getCount() > 0)
		{

			TaskListWidgetItem[] items = new TaskListWidgetItem[mTasksCursor.getCount()];
			int itemIndex = 0;

			while (mTasksCursor.moveToNext())
			{
				items[itemIndex] = new TaskListWidgetItem(TaskFieldAdapters.TASK_ID.get(mTasksCursor), TaskFieldAdapters.TITLE.get(mTasksCursor),
					TaskFieldAdapters.DUE.get(mTasksCursor), TaskFieldAdapters.LIST_COLOR.get(mTasksCursor), TaskFieldAdapters.IS_CLOSED.get(mTasksCursor));
				itemIndex++;
			}
			return items;
		}
		return null;
	}
}
