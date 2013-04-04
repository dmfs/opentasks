package org.dmfs.tasks.model.contraints;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;

import android.text.format.Time;


public class NotAfterContraint extends AbstractConstraint<Time>
{
	private final TimeFieldAdapter mTimeAdapter;


	public NotAfterContraint(TimeFieldAdapter adapter)
	{
		mTimeAdapter = adapter;
	}


	@Override
	public void apply(ContentSet values, Time object)
	{
		Time notAfterThisTime = mTimeAdapter.get(values);
		if (notAfterThisTime != null && object != null)
		{
			if (object.after(notAfterThisTime))
			{
				object.set(notAfterThisTime);
			}
		}
	}

}
