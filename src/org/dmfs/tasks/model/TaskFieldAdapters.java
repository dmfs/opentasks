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

package org.dmfs.tasks.model;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.adapters.BooleanFieldAdapter;
import org.dmfs.tasks.model.adapters.IntegerFieldAdapter;
import org.dmfs.tasks.model.adapters.StringFieldAdapter;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.model.adapters.TimezoneFieldAdapter;
import org.dmfs.tasks.model.adapters.UrlFieldAdapter;
import org.dmfs.tasks.model.contraints.NotAfter;
import org.dmfs.tasks.model.contraints.NotBefore;


/**
 * This class holds a static reference for all field adapters. That allows us to use them across different models.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class TaskFieldAdapters
{
	/**
	 * Adapter for the all day flag of a task.
	 */
	public final static BooleanFieldAdapter ALLDAY = new BooleanFieldAdapter(Tasks.IS_ALLDAY);

	/**
	 * Adapter for the status of a task.
	 */
	public final static IntegerFieldAdapter STATUS = new IntegerFieldAdapter(Tasks.STATUS, Tasks.STATUS_NEEDS_ACTION);

	/**
	 * Adapter for the percent complete value of a task.
	 */
	public final static IntegerFieldAdapter PERCENT_COMPLETE = new IntegerFieldAdapter(Tasks.PERCENT_COMPLETE);

	/**
	 * Adapter for the priority value of a task.
	 */
	public final static IntegerFieldAdapter PRIORITY = new IntegerFieldAdapter(Tasks.PRIORITY);

	/**
	 * Adapter for the classification value of a task.
	 */
	public final static IntegerFieldAdapter CLASSIFICATION = new IntegerFieldAdapter(Tasks.CLASSIFICATION);

	/**
	 * Adapter for the list name of a task.
	 */
	public final static StringFieldAdapter LIST_NAME = new StringFieldAdapter(Tasks.LIST_NAME);

	/**
	 * Adapter for the account name of a task.
	 */
	public final static StringFieldAdapter ACCOUNT_NAME = new StringFieldAdapter(Tasks.ACCOUNT_NAME);

	/**
	 * Adapter for the title of a task.
	 */
	public final static StringFieldAdapter TITLE = new StringFieldAdapter(Tasks.TITLE);

	/**
	 * Adapter for the location of a task.
	 */
	public final static StringFieldAdapter LOCATION = new StringFieldAdapter(Tasks.LOCATION);

	/**
	 * Adapter for the description of a task.
	 */
	public final static StringFieldAdapter DESCRIPTION = new StringFieldAdapter(Tasks.DESCRIPTION);

	/**
	 * Private adapter for the start date of a task. We need this to reference DTSTART from DUE.
	 */
	private final static TimeFieldAdapter _DTSTART = new TimeFieldAdapter(Tasks.DTSTART, Tasks.TZ, Tasks.IS_ALLDAY);

	/**
	 * Adapter for the due date of a task.
	 */
	public final static TimeFieldAdapter DUE = (TimeFieldAdapter) new TimeFieldAdapter(Tasks.DUE, Tasks.TZ, Tasks.IS_ALLDAY).addContraint(new NotBefore(
		_DTSTART));

	/**
	 * Adapter for the start date of a task.
	 */
	public final static TimeFieldAdapter DTSTART = (TimeFieldAdapter) _DTSTART.addContraint(new NotAfter(DUE));

	/**
	 * Adapter for the completed date of a task.
	 */
	public final static TimeFieldAdapter COMPLETED = new TimeFieldAdapter(Tasks.COMPLETED, null, null);

	/**
	 * Adapter for the time zone of a task.
	 */
	public final static TimezoneFieldAdapter TIMEZONE = new TimezoneFieldAdapter(Tasks.TZ, Tasks.IS_ALLDAY, Tasks.DUE);

	/**
	 * Adapter for the URL of a task.
	 */
	public final static UrlFieldAdapter URL = new UrlFieldAdapter(TaskContract.Tasks.URL);

	/**
	 * Adapter for the Color of the task.
	 * */
	public final static IntegerFieldAdapter LIST_COLOR = new IntegerFieldAdapter(TaskContract.Tasks.LIST_COLOR);

	/**
	 * Adpater for the ID of the task.
	 * */
	public static final IntegerFieldAdapter TASK_ID = new IntegerFieldAdapter(TaskContract.Tasks._ID);


	/**
	 * Private constructor to prevent instantiation.
	 */
	private TaskFieldAdapters()
	{
	}
}
