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

import org.dmfs.tasks.model.ContentSet;

import android.database.Cursor;


/**
 * Knows how to load and store descriptions from/to a combined description/check list field.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class DescriptionStringFieldAdapter extends StringFieldAdapter
{

	/**
	 * Constructor for a new DescriptionStringFieldAdapter without default value.
	 * 
	 * @param fieldName
	 *            The name of the field to use when loading or storing the value.
	 */
	public DescriptionStringFieldAdapter(String fieldName)
	{
		super(fieldName);
	}


	/**
	 * Constructor for a new StringFieldAdapter with default value.
	 * 
	 * @param fieldName
	 *            The name of the field to use when loading or storing the value.
	 * @param defaultValue
	 *            The default value.
	 */
	public DescriptionStringFieldAdapter(String fieldName, String defaultValue)
	{
		super(fieldName, defaultValue);
	}


	@Override
	public String get(ContentSet values)
	{
		return extractDescription(super.get(values));
	}


	@Override
	public String get(Cursor cursor)
	{
		return extractDescription(super.get(cursor));
	}


	@Override
	public void set(ContentSet values, String value)
	{
		String oldValue = super.get(values);
		if (oldValue != null && oldValue.length() > 0)
		{
			String oldDescription = extractDescription(oldValue);
			String oldChecklist = oldValue.substring(oldDescription.length());

			// store the new description with the old check list
			super.set(values, oldChecklist.length() == 0 ? value : oldChecklist.startsWith("\n") ? value + oldChecklist : value + "\n" + oldChecklist);
		}
		else
		{
			// there was no old check list
			super.set(values, value);
		}
	}


	static String extractDescription(String value)
	{
		if (value == null || value.length() < 3)
		{
			return value;
		}

		if (value.charAt(0) == '[' && value.charAt(2) == ']')
		{
			char checkmark = value.charAt(1);
			if (checkmark == ' ' || checkmark == 'x' || checkmark == 'X')
			{
				// value doesn't contain a description, only a check list
				return "";
			}
		}

		int valueLen = value.length();
		int checklistpos = -1;
		while ((checklistpos = value.indexOf("\n[", checklistpos + 1)) >= 0)
		{
			if (checklistpos + 2 < valueLen && value.charAt(checklistpos + 3) == ']')
			{
				char checkmark = value.charAt(checklistpos + 2);
				if (checkmark == ' ' || checkmark == 'x' || checkmark == 'X')
				{
					// found a check list
					if (checklistpos > 0 && value.charAt(checklistpos - 1) == 0x0d)
					{
						// the list was separated by a CR LF sequence, remove the CR
						--checklistpos;
					}
					return value.substring(0, checklistpos);
				}
			}
		}

		// didn't find a valid check list
		return value;
	}
}
