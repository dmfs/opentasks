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

import java.util.ArrayList;


public class TaskItemGroup
{
	private ArrayList<TaskItem> itemGroup;
	private String listName;
	private int groupColor;
	private int listId;


	public TaskItemGroup(int id, String name, int color)
	{
		itemGroup = new ArrayList<TaskItem>();
		listName = name;
		groupColor = color;
		listId = id;
	}


	public String getName()
	{
		return listName;
	}


	public int getColor()
	{
		return groupColor;
	}


	public TaskItem getChild(int position)
	{
		return itemGroup.get(position);
	}

	public void putChild(TaskItem i){
		itemGroup.add(i);
	}

	public int getId()
	{
		return listId;
	}
	
	public int getSize(){
		return itemGroup.size();
	}

}
