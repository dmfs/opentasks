package org.dmfs.tasks.utils;

/**
 * A listener that is invoked whenever a time event occurs.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 */
public interface TimeUpdateListener
{
	public void onTimeUpdate(TimeChangeManager manager);
}
