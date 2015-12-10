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

package org.dmfs.tasks.groupings.cursorloaders;

import android.database.Cursor;


/**
 * A factory that builds shiny new {@link Cursor}s with time ranges.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class AbstractCustomCursorFactory
{
	protected String[] mProjection;


	/**
	 * Initialize the factory with the given projection.
	 * 
	 * @param projection
	 *            An array of column names.
	 */
	public AbstractCustomCursorFactory(String[] projection)
	{
		mProjection = projection;
	}


	/**
	 * Get a new {@link Cursor} from this factory.
	 * 
	 * @return A {@link Cursor}.
	 */
	public abstract Cursor getCursor();
}
