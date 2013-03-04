package org.dmfs.tasks.groups;

import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;


public interface Common
{

	/**
	 * The projection we use when we load instances. We don't need every detail of a task here.
	 */
	public final static String[] INSTANCE_PROJECTION = new String[] { Instances.INSTANCE_START, Instances.INSTANCE_DURATION, Instances.INSTANCE_DUE,
		Instances.IS_ALLDAY, Instances.TZ, Instances.TITLE, Instances.LIST_COLOR, Instances.PRIORITY, Instances.LIST_ID, Instances.TASK_ID, Instances._ID,
		Instances.STATUS, Instances.COMPLETED };

	/**
	 * An adapter to load the due date from the instances projection.
	 */
	public final static TimeFieldAdapter DUE_ADAPTER = new TimeFieldAdapter(Instances.INSTANCE_DUE, Instances.TZ, Instances.IS_ALLDAY);

}
