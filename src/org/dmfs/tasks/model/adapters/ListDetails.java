/*
 * ListDetails.java
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

package org.dmfs.tasks.model.adapters;

/**
 * This type holds all the information that's necessary to show which list a task belongs to.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ListDetails
{
	public final int listId;
	public final String listName;
	public final String listAccountName;
	public final int listColor;


	/**
	 * Initialize a new ListDetails instance.
	 * 
	 * @param listId
	 *            The id if the list a task belongs to.
	 * @param listName
	 *            The name of the list.
	 * @param listAccountName
	 *            The account name of the list.
	 * @param listColor
	 *            The color of the list.
	 */
	public ListDetails(int listId, String listName, String listAccountName, int listColor)
	{
		this.listId = listId;
		this.listName = listName;
		this.listAccountName = listAccountName;
		this.listColor = listColor;
	}
}
