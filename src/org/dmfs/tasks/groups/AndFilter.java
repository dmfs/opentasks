package org.dmfs.tasks.groups;

import java.util.ArrayList;
import java.util.Arrays;


public final class AndFilter extends AbstractFilter
{
	private final AbstractFilter[] mFilters;


	public AndFilter(AbstractFilter... filters)
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
				selection.append(") AND (");
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
}
