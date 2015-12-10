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

/**
 * Interface for listeners that listen to changes in a {@link ContentSet}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface OnContentChangeListener
{
	/**
	 * Called whenever a specific key in a {@link ContentSet} has changed.
	 * 
	 * @param contentSet
	 *            The {@link ContentSet} that contains the changed key.
	 */
	public void onContentChanged(ContentSet contentSet);


	/**
	 * Called whenever the {@link ContentSet} has been (re-)loaded.
	 * 
	 * @param contentSet
	 *            The {@link ContentSet} that has been reloaded.
	 */
	public void onContentLoaded(ContentSet contentSet);
}
