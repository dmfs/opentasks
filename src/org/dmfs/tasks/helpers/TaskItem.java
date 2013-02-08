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
package org.dmfs.tasks.helpers;

import android.text.format.Time;

public class TaskItem
{
	private int taskId, taskColor;
	private String taskTitle;
	private Time dueDate;

	public TaskItem(int i, int c, String t, Time d)
	{
		taskId = i;
		taskColor = c;
		dueDate = d;
		taskTitle = t;
	}


	public String getTaskTitle()
	{
		return taskTitle;
	}


	public int getTaskId()
	{
		return taskId;
	}


	public int getTaskColor()
	{
		return taskColor;
	}
	
	public Time getDueDate(){
		return dueDate;
	}
	
}
