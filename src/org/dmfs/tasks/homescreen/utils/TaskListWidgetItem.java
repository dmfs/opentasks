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

import android.text.format.Time;


/**
 * This Class is used to for storing data of a single task in the task list widget.
 * 
 * @author Arjun Naik<arjun@arjunnaik.in>
 */
public class TaskListWidgetItem
{

	/** The task title. */
	private String mTaskTitle;

	/** The due date. */
	private Time mDueDate;

	/** The task color. */
	private int mTaskColor;

	/** The task ID. */
	private int mTaskId;

	/** The flag to indicate if task is closed. */
	private boolean mIsClosed;


	/**
	 * Instantiates a new task list widget item.
	 * 
	 * @param id
	 *            the id of the task
	 * @param title
	 *            the title of the task
	 * @param due
	 *            the due date of the task
	 * @param color
	 *            the color of the list of the task
	 * @param isClosed
	 *            the flag to indicate if closed
	 */
	public TaskListWidgetItem(int id, String title, Time due, int color, boolean isClosed)
	{
		mTaskId = id;
		mTaskTitle = title;
		mDueDate = due;
		mTaskColor = color;
		mIsClosed = isClosed;
	}


	/**
	 * Gets the task color.
	 * 
	 * @return the task color
	 */
	public int getTaskColor()
	{
		return mTaskColor;
	}


	/**
	 * Sets the task color.
	 * 
	 * @param taskColor
	 *            the new task color
	 */
	public void setTaskColor(int taskColor)
	{
		mTaskColor = taskColor;
	}


	/**
	 * Gets the due date.
	 * 
	 * @return the due date
	 */
	public Time getDueDate()
	{
		return mDueDate;
	}


	/**
	 * Sets the due date.
	 * 
	 * @param dueDate
	 *            the new due date
	 */
	public void setDueDate(Time dueDate)
	{
		mDueDate = dueDate;
	}


	/**
	 * Gets the task title.
	 * 
	 * @return the task title
	 */
	public String getTaskTitle()
	{
		return mTaskTitle;
	}


	/**
	 * Sets the task title.
	 * 
	 * @param taskTitle
	 *            the new task title
	 */
	public void setTaskTitle(String taskTitle)
	{
		mTaskTitle = taskTitle;
	}


	/**
	 * Gets the task id.
	 * 
	 * @return the task id
	 */
	public long getTaskId()
	{
		return mTaskId;
	}


	/**
	 * Gets the checks if is closed.
	 * 
	 * @return the checks if is closed
	 */
	public boolean getIsClosed()
	{
		return mIsClosed;
	}


	/**
	 * Sets the checks if is closed.
	 * 
	 * @param isClosed
	 *            the new checks if is closed
	 */
	public void setIsClosed(boolean isClosed)
	{
		mIsClosed = isClosed;
	}
}
