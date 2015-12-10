/*
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

package org.dmfs.tasks.utils;

import android.database.Cursor;


/**
 * A listener that is notified when an {@link ExpandableGroupDescriptorAdapter} has loaded a child cursor.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface OnChildLoadedListener
{
	/**
	 * Called when the cursor for the children of group <code>pos</code> has been loaded.
	 * 
	 * @param pos
	 *            The position of the group whose child cursor has been loaded.
	 * @param childCursor
	 *            The cursor of the children for the group.
	 */
	public void onChildLoaded(int pos, Cursor childCursor);
}
