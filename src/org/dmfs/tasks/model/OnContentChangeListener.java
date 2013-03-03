/*
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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
