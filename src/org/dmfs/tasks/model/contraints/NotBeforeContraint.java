package org.dmfs.tasks.model.contraints;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;

import android.text.format.Time;


public class NotBeforeContraint extends AbstractConstraint<Time>
{
	private final TimeFieldAdapter mTimeAdapter;


	public NotBeforeContraint(TimeFieldAdapter adapter)
	{
		mTimeAdapter = adapter;
	}


	@Override
	public void apply(ContentSet values, Time object)
	{
		Time notBeforeThisTime = mTimeAdapter.get(values);
		if (notBeforeThisTime != null && object != null)
		{
			if (object.before(notBeforeThisTime))
			{
				object.set(notBeforeThisTime);
			}
		}
	}

}
