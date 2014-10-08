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

package org.dmfs.tasks.groupings.filters;

import java.util.List;


/**
 * A filter that filters by a constant selection.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ConstantFilter implements AbstractFilter
{
	private final String mSelection;
	private final String[] mSelectionArgs;


	/**
	 * Creates a ConstantFilter.
	 * 
	 * @param selection
	 *            The selection string.
	 * @param selectionArgs
	 *            The positional selection arguments.
	 */
	public ConstantFilter(String selection, String... selectionArgs)
	{
		mSelection = selection;
		mSelectionArgs = selectionArgs;
	}


	@Override
	public void getSelection(StringBuilder stringBuilder)
	{
		if (mSelection != null)
		{
			stringBuilder.append(mSelection);
		}
	}


	@Override
	public void getSelectionArgs(List<String> selectionArgs)
	{
		if (mSelectionArgs != null)
		{
			// append all arguments, if any
			for (String arg : mSelectionArgs)
			{
				selectionArgs.add(arg);
			}
		}
	}
}
