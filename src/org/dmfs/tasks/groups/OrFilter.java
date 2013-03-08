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

package org.dmfs.tasks.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A filter that joins a list of {@link AbstractFilter}s using the "OR" operator.
 * 
 * TODO: get rid of duplicate code with {@link AndFilter}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class OrFilter extends AbstractFilter
{
	private final AbstractFilter[] mFilters;


	public OrFilter(AbstractFilter... filters)
	{
		mFilters = filters;
	}


	@Override
	public String getSelection()
	{
		AbstractFilter[] filters = mFilters;
		if (filters.length == 0)
		{
			// return a valid filter that always matches
			return "1=1";
		}

		StringBuilder selection = new StringBuilder(filters.length * 24); // assuming an average of 24 characters per filter

		boolean first = true;
		for (AbstractFilter filter : filters)
		{
			if (first)
			{
				first = false;
				selection.append("(");
			}
			else
			{
				selection.append(") OR (");
			}
			selection.append(filter.getSelection());
		}
		selection.append(")");

		return selection.toString();
	}


	@Override
	public String[] getSelectionArgs()
	{
		AbstractFilter[] filters = mFilters;
		if (filters.length == 0)
		{
			return new String[] {};
		}
		ArrayList<String> result = new ArrayList<String>(filters.length + 8);
		for (AbstractFilter filter : filters)
		{
			result.addAll(Arrays.asList(filter.getSelectionArgs()));
		}
		return result.toArray(new String[result.size()]);
	}


	@Override
	public void getSelection(StringBuilder stringBuilder)
	{
		AbstractFilter[] filters = mFilters;
		if (filters.length == 0)
		{
			// return a valid filter that always matches
			stringBuilder.append("1=1");
			return;
		}

		boolean first = true;
		for (AbstractFilter filter : filters)
		{
			if (first)
			{
				first = false;
				stringBuilder.append("(");
			}
			else
			{
				stringBuilder.append(") OR (");
			}
			stringBuilder.append(filter.getSelection());
		}
		stringBuilder.append(")");
	}


	@Override
	public void getSelectionArgs(List<String> selectionArgs)
	{
		for (AbstractFilter filter : mFilters)
		{
			selectionArgs.addAll(Arrays.asList(filter.getSelectionArgs()));
		}
	}
}
