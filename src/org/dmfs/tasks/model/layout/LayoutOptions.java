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

package org.dmfs.tasks.model.layout;

import java.util.HashMap;
import java.util.Map;


public final class LayoutOptions
{
	private Map<String, Object> mOptionMap;


	LayoutOptions()
	{
	}


	void put(String key, boolean value)
	{
		if (mOptionMap == null)
		{
			mOptionMap = new HashMap<String, Object>();
		}
		mOptionMap.put(key, value);
	}


	public boolean getBoolean(String key, boolean defaultValue)
	{
		if (mOptionMap == null)
		{
			return defaultValue;
		}
		Object value = mOptionMap.get(key);
		return value instanceof Boolean && (Boolean) value || (!(value instanceof Boolean) && defaultValue);
	}
}
