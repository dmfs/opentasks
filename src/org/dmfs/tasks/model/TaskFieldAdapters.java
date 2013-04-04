package org.dmfs.tasks.model;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.adapters.BooleanFieldAdapter;
import org.dmfs.tasks.model.adapters.IntegerFieldAdapter;
import org.dmfs.tasks.model.adapters.StringFieldAdapter;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.model.adapters.TimezoneFieldAdapter;
import org.dmfs.tasks.model.adapters.UrlFieldAdapter;


public interface TaskFieldAdapters
{
	BooleanFieldAdapter ALLDAY = new BooleanFieldAdapter(Tasks.IS_ALLDAY);

	IntegerFieldAdapter STATUS = new IntegerFieldAdapter(Tasks.STATUS, Tasks.STATUS_NEEDS_ACTION);
	IntegerFieldAdapter PERCENT_COMPLETE = new IntegerFieldAdapter(Tasks.PERCENT_COMPLETE);
	IntegerFieldAdapter PRIORITY = new IntegerFieldAdapter(Tasks.PRIORITY);
	IntegerFieldAdapter CLASSIFICATION = new IntegerFieldAdapter(Tasks.CLASSIFICATION);

	StringFieldAdapter LIST_NAME = new StringFieldAdapter(Tasks.LIST_NAME);
	StringFieldAdapter ACCOUNT_NAME = new StringFieldAdapter(Tasks.ACCOUNT_NAME);
	StringFieldAdapter TITLE = new StringFieldAdapter(Tasks.TITLE);
	StringFieldAdapter LOCATION = new StringFieldAdapter(Tasks.LOCATION);
	StringFieldAdapter DESCRIPTION = new StringFieldAdapter(Tasks.DESCRIPTION);

	TimeFieldAdapter DTSTART = new TimeFieldAdapter(Tasks.DTSTART, Tasks.TZ, Tasks.IS_ALLDAY);
	TimeFieldAdapter DUE = new TimeFieldAdapter(Tasks.DUE, Tasks.TZ, Tasks.IS_ALLDAY);
	TimeFieldAdapter COMPLETED = new TimeFieldAdapter(Tasks.COMPLETED, null, null);

	TimezoneFieldAdapter TIMEZONE = new TimezoneFieldAdapter(Tasks.TZ, Tasks.IS_ALLDAY, Tasks.DUE);

	UrlFieldAdapter URL = new UrlFieldAdapter(TaskContract.Tasks.URL);
}
