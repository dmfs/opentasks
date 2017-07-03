/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.provider.tasks.handler;

import org.dmfs.provider.tasks.TaskContract.Property.Alarm;
import org.dmfs.provider.tasks.TaskContract.Property.Category;
import org.dmfs.provider.tasks.TaskContract.Property.Relation;


/**
 * A factory that creates the matching {@link PropertyHandler} for the given mimetype.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class PropertyHandlerFactory
{
	private final static PropertyHandler CATEGORY_HANDLER = new CategoryHandler();
	private final static PropertyHandler ALARM_HANDLER = new AlarmHandler();
	private final static PropertyHandler RELATION_HANDLER = new RelationHandler();
	private final static PropertyHandler DEFAULT_PROPERTY_HANDLER = new DefaultPropertyHandler();


	/**
	 * Creates a specific {@link PropertyHandler}.
	 * 
	 * @param mimeType
	 *            The mimetype of the property.
	 * @return The matching {@link PropertyHandler} for the given mimetype or <code>null</code>
	 */
	public static PropertyHandler get(String mimeType)
	{
		if (Category.CONTENT_ITEM_TYPE.equals(mimeType))
		{
			return CATEGORY_HANDLER;
		}
		if (Alarm.CONTENT_ITEM_TYPE.equals(mimeType))
		{
			return ALARM_HANDLER;
		}
		if (Relation.CONTENT_ITEM_TYPE.equals(mimeType))
		{
			return RELATION_HANDLER;
		}
		return DEFAULT_PROPERTY_HANDLER;
	}
}
