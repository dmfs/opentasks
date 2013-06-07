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

package org.dmfs.tasks.utils;

import org.dmfs.tasks.model.Model;


/**
 * A listener that is notified when a {@link AsyncModelLoader} has loaded a model.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface OnModelLoadedListener
{
	/**
	 * Called when a model has been loaded.
	 * 
	 * @param model
	 *            The {@link Model} for the requested account type or {@code null} if no such account type exists.
	 */
	public void onModelLoaded(Model model);
}
