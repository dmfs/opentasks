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

package org.dmfs.tasks.groupings;

import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.utils.ExpandableChildDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;


/**
 * An abstract factory to create {@link ExpandableGroupDescriptor}s.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class AbstractGroupingFactory
{

	/**
	 * The projection we use when we load instances. We don't need every detail of a task here. This is used by all groupings.
	 */
	public final static String[] INSTANCE_PROJECTION = new String[] { Instances.INSTANCE_START, Instances.INSTANCE_DURATION, Instances.INSTANCE_DUE,
		Instances.IS_ALLDAY, Instances.TZ, Instances.TITLE, Instances.LIST_COLOR, Instances.PRIORITY, Instances.LIST_ID, Instances.TASK_ID, Instances._ID,
		Instances.STATUS, Instances.COMPLETED, Instances.IS_CLOSED, Instances.PERCENT_COMPLETE, Instances.ACCOUNT_NAME, Instances.ACCOUNT_TYPE,
		Instances.DESCRIPTION };

	/**
	 * An adapter to load the due date from the instances projection. This is used by most groupings
	 */
	public final static TimeFieldAdapter INSTANCE_DUE_ADAPTER = new TimeFieldAdapter(Instances.INSTANCE_DUE, Instances.TZ, Instances.IS_ALLDAY);

	/**
	 * An adapter to load the start date from the instances projection. This is used by most groupings
	 */
	public final static TimeFieldAdapter INSTANCE_START_ADAPTER = new TimeFieldAdapter(Instances.INSTANCE_START, Instances.TZ, Instances.IS_ALLDAY);

	/**
	 * The authority of the content provider.
	 */
	private final String mAuthority;

	/**
	 * The instance of the {@link ExpandableGroupDescriptor}. This is created on demand in a lazy manner.
	 */
	private ExpandableGroupDescriptor mDescriptorInstance;


	public AbstractGroupingFactory(String authority)
	{
		mAuthority = authority;
	}


	/**
	 * Returns an {@link ExpandableChildDescriptor} for this grouping and the given authority.
	 * 
	 * @param authority
	 *            The authority.
	 * @return An {@link ExpandableChildDescriptor}.
	 */
	abstract ExpandableChildDescriptor makeExpandableChildDescriptor(String authority);


	/**
	 * Returns an {@link ExpandableGroupDescriptor} for this grouping and the given authority.
	 * 
	 * @param authority
	 *            The authority.
	 * @return An {@link ExpandableGroupDescriptor}.
	 */
	abstract ExpandableGroupDescriptor makeExpandableGroupDescriptor(String authority);


	/**
	 * Return an {@link ExpandableGroupDescriptor} for this grouping.
	 * <p>
	 * This method is not synchronized because it's intended to be called from the main thread only.
	 * </p>
	 * 
	 * @return An {@link ExpandableGroupDescriptor}.
	 */
	public ExpandableGroupDescriptor getExpandableGroupDescriptor()
	{
		if (mDescriptorInstance == null)
		{
			mDescriptorInstance = makeExpandableGroupDescriptor(mAuthority);
		}
		return mDescriptorInstance;
	}


	public abstract int getId();
}
