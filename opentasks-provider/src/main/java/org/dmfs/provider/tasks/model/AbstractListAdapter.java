/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.provider.tasks.model;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.model.adapters.FieldAdapter;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;


/**
 * An abstract implementation of a {@link ListAdapter} to server as the base for more concrete adapters.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class AbstractListAdapter implements ListAdapter
{
	private final ContentValues mState = new ContentValues(10);


	@Override
	public Uri uri(String authority)
	{
		return ContentUris.withAppendedId(TaskContract.TaskLists.getContentUri(authority), id());
	}


	@Override
	public <T> T getState(FieldAdapter<T, ListAdapter> stateFieldAdater)
	{
		return stateFieldAdater.getFrom(mState);
	}


	@Override
	public <T> void setState(FieldAdapter<T, ListAdapter> stateFieldAdater, T value)
	{
		stateFieldAdater.setIn(mState, value);
	}
}
