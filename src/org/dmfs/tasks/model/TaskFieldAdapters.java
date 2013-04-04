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


public final class TaskFieldAdapters
{
	public final static BooleanFieldAdapter ALLDAY = new BooleanFieldAdapter(Tasks.IS_ALLDAY);

	public final static IntegerFieldAdapter STATUS = new IntegerFieldAdapter(Tasks.STATUS, Tasks.STATUS_NEEDS_ACTION);
	public final static IntegerFieldAdapter PERCENT_COMPLETE = new IntegerFieldAdapter(Tasks.PERCENT_COMPLETE);
	public final static IntegerFieldAdapter PRIORITY = new IntegerFieldAdapter(Tasks.PRIORITY);
	public final static IntegerFieldAdapter CLASSIFICATION = new IntegerFieldAdapter(Tasks.CLASSIFICATION);

	public final static StringFieldAdapter LIST_NAME = new StringFieldAdapter(Tasks.LIST_NAME);
	public final static StringFieldAdapter ACCOUNT_NAME = new StringFieldAdapter(Tasks.ACCOUNT_NAME);
	public final static StringFieldAdapter TITLE = new StringFieldAdapter(Tasks.TITLE);
	public final static StringFieldAdapter LOCATION = new StringFieldAdapter(Tasks.LOCATION);
	public final static StringFieldAdapter DESCRIPTION = new StringFieldAdapter(Tasks.DESCRIPTION);

	private final static TimeFieldAdapter _DTSTART = new TimeFieldAdapter(Tasks.DTSTART, Tasks.TZ, Tasks.IS_ALLDAY);
	public final static TimeFieldAdapter DUE = (TimeFieldAdapter) new TimeFieldAdapter(Tasks.DUE, Tasks.TZ, Tasks.IS_ALLDAY).addContraint(new NotBefore(
		_DTSTART));
	public final static TimeFieldAdapter COMPLETED = new TimeFieldAdapter(Tasks.COMPLETED, null, null);
	public final static TimeFieldAdapter DTSTART = (TimeFieldAdapter) _DTSTART.addContraint(new NotAfter(DUE));

	public final static TimezoneFieldAdapter TIMEZONE = new TimezoneFieldAdapter(Tasks.TZ, Tasks.IS_ALLDAY, Tasks.DUE);

	public final static UrlFieldAdapter URL = new UrlFieldAdapter(TaskContract.Tasks.URL);


	private TaskFieldAdapters()
	{
	}
}
