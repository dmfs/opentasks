/*
 * ListDetailsFieldAdapter.java
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

import android.content.ContentValues;


/**
 * A ListTitleFiledAdapter knows how to map the list detail columns to a {@link ListDetails} instance.
 * <p>
 * <strong>Note: </strong>This adapter does not support the {@link #set(ContentValues, ListDetails)} operation and will throw an
 * {@link UnsupportedOperationException} when it's called.
 * </p>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ListDetailsFieldAdapter extends FieldAdapter<ListDetails>
{

	/**
	 * The field name that holds the list id.
	 */
	private final String mListIdFieldName;

	/**
	 * The field name that holds the list title.
	 */
	private final String mTitleFieldName;

	/**
	 * The field name that holds the account name.
	 */
	private final String mAccountNameFieldName;

	/**
	 * The field name that holds the list color.
	 */
	private final String mColorFieldName;


	/**
	 * Create a new {@link ListDetailsFieldAdapter}.
	 * 
	 * @param listIdFileName
	 *            The column that holds the list id.
	 * @param titleFieldName
	 *            The column that holds the list name.
	 * @param accountNameFieldName
	 *            The column that holds the account name.
	 * @param colorFieldName
	 *            The column that holds the list color.
	 */
	public ListDetailsFieldAdapter(String listIdFileName, String titleFieldName, String accountNameFieldName, String colorFieldName)
	{
		mListIdFieldName = listIdFileName;
		mTitleFieldName = titleFieldName;
		mAccountNameFieldName = accountNameFieldName;
		mColorFieldName = colorFieldName;
	}


	@Override
	public ListDetails get(ContentValues values)
	{
		return new ListDetails(values.getAsInteger(mListIdFieldName), values.getAsString(mTitleFieldName), values.getAsString(mAccountNameFieldName),
			values.getAsInteger(mColorFieldName));
	}


	@Override
	public ListDetails getDefault(ContentValues values)
	{
		// return null, it doesn't make sense to return a default here
		return null;
	}


	@Override
	public void set(ContentValues values, ListDetails value)
	{
		throw new UnsupportedOperationException("set(...) is not supported by a ListDetailsFieldAdapter.");
	}

}
