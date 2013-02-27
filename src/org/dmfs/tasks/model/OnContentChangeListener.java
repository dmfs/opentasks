package org.dmfs.tasks.model;

public interface OnContentChangeListener
{
	/**
	 * Called whenever a specific key in the {@link ContentSet} {@code contentSet} has changed.
	 * 
	 * @param contentSet
	 *            The {@link ContentSet} that contains the chanegd key.
	 * @param key
	 *            The key that has changed or <code>null</code> if all keys have changed (for example when the ContentSet has been initialized with values).
	 */
	public void onContentChanged(ContentSet contentSet, String key);
}
