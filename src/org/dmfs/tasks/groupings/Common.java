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

package org.dmfs.tasks.groupings;

import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;


/**
 * Just a few static fields used by all groupings.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface Common
{

	/**
	 * The projection we use when we load instances. We don't need every detail of a task here.
	 */
	public final static String[] INSTANCE_PROJECTION = new String[] { Instances.INSTANCE_START, Instances.INSTANCE_DURATION, Instances.INSTANCE_DUE,
		Instances.IS_ALLDAY, Instances.TZ, Instances.TITLE, Instances.LIST_COLOR, Instances.PRIORITY, Instances.LIST_ID, Instances.TASK_ID, Instances._ID,
		Instances.STATUS, Instances.COMPLETED, Instances.IS_CLOSED };

	/**
	 * An adapter to load the due date from the instances projection.
	 */
	public final static TimeFieldAdapter DUE_ADAPTER = new TimeFieldAdapter(Instances.INSTANCE_DUE, Instances.TZ, Instances.IS_ALLDAY);

	/**
	 * An adapter to load the start date from the instances projection.
	 */
	public final static TimeFieldAdapter START_DATE_ADAPTER = new TimeFieldAdapter(Instances.INSTANCE_START, Instances.TZ, Instances.IS_ALLDAY);

}
