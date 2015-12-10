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

package org.dmfs.tasks.model.layout;

import java.util.HashMap;
import java.util.Map;


/**
 * A helper to store the layout options used when rendering the task details and the task editor.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class LayoutOptions
{
	private Map<String, Object> mOptionMap;


	LayoutOptions()
	{
	}


	/**
	 * Put a boolean option.
	 * 
	 * @param key
	 *            The name of the option.
	 * @param value
	 *            The value of the option.
	 */
	void put(String key, boolean value)
	{
		if (mOptionMap == null)
		{
			mOptionMap = new HashMap<String, Object>();
		}
		mOptionMap.put(key, value);
	}


	/**
	 * Put a generic option.
	 * 
	 * @param key
	 *            The name of the option.
	 * @param value
	 *            The value of the option.
	 */
	void put(String key, Object value)
	{
		if (mOptionMap == null)
		{
			mOptionMap = new HashMap<String, Object>();
		}
		mOptionMap.put(key, value);
	}


	/**
	 * Put an int option.
	 * 
	 * @param key
	 *            The name of the option.
	 * @param value
	 *            The value of the option.
	 */
	void put(String key, int value)
	{
		if (mOptionMap == null)
		{
			mOptionMap = new HashMap<String, Object>();
		}
		mOptionMap.put(key, value);
	}


	/**
	 * Get the value of a boolean option.
	 * 
	 * @param key
	 *            The name of this option.
	 * @param defaultValue
	 *            The value to return if the option is not set yet.
	 * @return The value or defaultValue.
	 */
	public boolean getBoolean(String key, boolean defaultValue)
	{
		if (mOptionMap == null)
		{
			return defaultValue;
		}
		Object value = mOptionMap.get(key);
		return value instanceof Boolean && (Boolean) value || (!(value instanceof Boolean) && defaultValue);
	}


	/**
	 * Get the value of an int option.
	 * 
	 * @param key
	 *            The name of this option.
	 * @param defaultValue
	 *            The value to return if the option is not set yet.
	 * @return The value or defaultValue.
	 */
	public int getInt(String key, int defaultValue)
	{
		if (mOptionMap == null)
		{
			return defaultValue;
		}
		Object value = mOptionMap.get(key);
		if (value instanceof Integer)
		{
			return (Integer) value;
		}
		else
		{
			return defaultValue;
		}
	}
}
