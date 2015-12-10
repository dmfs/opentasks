/*
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

import java.util.ArrayList;
import java.util.List;

import org.dmfs.tasks.model.CheckListItem;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * Knows how to load and store check list from/to a combined description/check list field.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ChecklistFieldAdapter extends FieldAdapter<List<CheckListItem>>
{

	/**
	 * The field name this adapter uses to store the values.
	 */
	private final String mFieldName;

	/**
	 * The default value, if any.
	 */
	private final List<CheckListItem> mDefaultValue;


	/**
	 * Constructor for a new StringFieldAdapter without default value.
	 * 
	 * @param fieldName
	 *            The name of the field to use when loading or storing the value.
	 */
	public ChecklistFieldAdapter(String fieldName)
	{
		this(fieldName, null);
	}


	/**
	 * Constructor for a new StringFieldAdapter without default value.
	 * 
	 * @param fieldName
	 *            The name of the field to use when loading or storing the value.
	 * @param defaultValue
	 *            The default check list
	 */
	public ChecklistFieldAdapter(String fieldName, List<CheckListItem> defaultValue)
	{
		if (fieldName == null)
		{
			throw new IllegalArgumentException("fieldName must not be null");
		}
		mFieldName = fieldName;
		mDefaultValue = defaultValue;
	}


	@Override
	public List<CheckListItem> get(ContentSet values)
	{
		// return the check list
		return extractCheckList(values.getAsString(mFieldName));
	}


	@Override
	public List<CheckListItem> get(Cursor cursor)
	{
		int columnIdx = cursor.getColumnIndex(mFieldName);
		if (columnIdx < 0)
		{
			throw new IllegalArgumentException("The fieldName column missing in cursor.");
		}
		return extractCheckList(cursor.getString(columnIdx));
	}


	@Override
	public List<CheckListItem> getDefault(ContentSet values)
	{
		return mDefaultValue;
	}


	@Override
	public void set(ContentSet values, List<CheckListItem> value)
	{
		String oldDescription = DescriptionStringFieldAdapter.extractDescription(values.getAsString(mFieldName));
		if (value != null && value.size() > 0)
		{
			StringBuilder sb = new StringBuilder(1024);
			if (oldDescription != null)
			{
				sb.append(oldDescription);
				sb.append("\n");
			}

			serializeCheckList(sb, value);

			values.put(mFieldName, sb.toString());
		}
		else
		{
			// store the current value just without check list
			values.put(mFieldName, oldDescription);
		}
	}


	@Override
	public void set(ContentValues values, List<CheckListItem> value)
	{
		String oldDescription = DescriptionStringFieldAdapter.extractDescription(values.getAsString(mFieldName));
		if (value != null && value.size() > 0)
		{
			StringBuilder sb = new StringBuilder(1024);
			if (oldDescription != null)
			{
				sb.append(oldDescription);
				sb.append("\n");
			}

			serializeCheckList(sb, value);

			values.put(mFieldName, sb.toString());
		}
		else
		{
			// store the current value just without check list
			values.put(mFieldName, oldDescription);
		}

	}


	@Override
	public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initalNotification)
	{
		values.addOnChangeListener(listener, mFieldName, initalNotification);
	}


	@Override
	public void unregisterListener(ContentSet values, OnContentChangeListener listener)
	{
		values.removeOnChangeListener(listener, mFieldName);
	}


	private static List<CheckListItem> extractCheckList(String value)
	{
		if (value != null && value.length() >= 3)
		{
			int checklistpos = -1;
			while ((checklistpos = value.indexOf("[", checklistpos + 1)) >= 0)
			{
				if (value.length() > checklistpos + 2 && value.charAt(checklistpos + 2) == ']' && (checklistpos == 0 || value.charAt(checklistpos - 1) == '\n'))
				{
					char checkmark = value.charAt(checklistpos + 1);
					if (checkmark == ' ' || checkmark == 'x' || checkmark == 'X')
					{
						return parseCheckList(value.substring(checklistpos));
					}
				}
			}
		}
		return new ArrayList<CheckListItem>(4);
	}


	private static List<CheckListItem> parseCheckList(String checklist)
	{
		List<CheckListItem> result = new ArrayList<CheckListItem>(16);
		String[] lines = checklist.split("\n");

		for (String line : lines)
		{
			line = line.trim();
			if (line.length() == 0)
			{
				// skip empty lines
				continue;
			}

			if (line.startsWith("[x]") || line.startsWith("[X]"))
			{
				result.add(new CheckListItem(true, line.substring(3).trim()));
			}
			else if (line.startsWith("[ ]"))
			{
				result.add(new CheckListItem(false, line.substring(3).trim()));
			}
			else
			{
				result.add(new CheckListItem(false, line));
			}
		}
		return result;
	}


	private static void serializeCheckList(StringBuilder sb, List<CheckListItem> checklist)
	{
		if (checklist == null || checklist.size() == 0)
		{
			return;
		}

		boolean first = true;
		for (CheckListItem item : checklist)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append('\n');
			}
			sb.append(item.checked ? "[x] " : "[ ] ");
			sb.append(item.text);
		}
	}

}
