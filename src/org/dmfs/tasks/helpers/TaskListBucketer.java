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

import java.util.Arrays;
import java.util.HashMap;

import android.graphics.Color;
import android.text.format.Time;


public class TaskListBucketer extends HashMap<String, TaskItemGroup>
{
	public static final String OVERDUE = "overdue";
	public static final String DUE_TODAY = "due_today";
	public static final String DUE_TOMORROW = "due_tomorrow";
	public static final String DUE_SOON = "due_soon";
	public static final String DUE_LATER = "due_later";
	public Time todayTime;


	public TaskListBucketer(Time tTime)
	{
		super();
		super.put(OVERDUE, new TaskItemGroup(1, "OverDue", Color.RED));
		super.put(DUE_TODAY, new TaskItemGroup(2, "Due Today", Color.GREEN));
		super.put(DUE_TOMORROW, new TaskItemGroup(3, "Due Tomorrow", Color.YELLOW));
		super.put(DUE_SOON, new TaskItemGroup(4, "Due Soon", Color.CYAN));
		super.put(DUE_LATER, new TaskItemGroup(5, "Due Later", Color.GRAY));
		todayTime = tTime;
	}


	public void put(TaskItem nTask){
		Time taskDD = nTask.getDueDate();
		if(taskDD == null){
			super.get(DUE_SOON).putChild(nTask);
		}
		else if(todayTime.after(taskDD)){
			super.get(OVERDUE).putChild(nTask);
			
		}
		else if(todayTime.year == taskDD.year && todayTime.month == taskDD.month && todayTime.monthDay == taskDD.monthDay){
			super.get(DUE_TODAY).putChild(nTask);
		}
		else {
			super.get(DUE_LATER).putChild(nTask);
		}
	}
	
	public TaskItemGroup[] getArray(){
		Object[] tigObjects = super.values().toArray();
		return Arrays.copyOf(tigObjects, tigObjects.length, TaskItemGroup[].class);
	}
}
