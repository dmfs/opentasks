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

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v4.content.CursorLoader;


/**
 * A simple cursor loader factory that returns {@link CursorLoader}s that return empty cursors. This is meant as a hack to be able to return <code>null</code>
 * values from onCreateLoader.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class EmptyCursorLoaderFactory extends CustomCursorLoader
{
	public EmptyCursorLoaderFactory(Context context, String[] projection)
	{
		super(context, new AbstractCustomCursorFactory(projection)
		{

			@Override
			public Cursor getCursor()
			{
				return new MatrixCursor(mProjection);
			}
		});
	}

}
