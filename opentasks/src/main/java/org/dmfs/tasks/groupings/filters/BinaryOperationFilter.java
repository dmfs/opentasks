/*
 * Copyright 2017 dmfs GmbH
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
 */

package org.dmfs.tasks.groupings.filters;

import java.util.List;


/**
 * A filter that joins a list of {@link AbstractFilter}s using the specified operator.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class BinaryOperationFilter implements AbstractFilter
{
    private final AbstractFilter[] mFilters;
    private final String mOperator;


    /**
     * Create a new filter that joins a list of {@link AbstractFilter}s using the specified operator.
     *
     * @param operator
     *         The operator to use (must be a valid binary boolean operator like "OR" or "AND").
     * @param filters
     *         A number of {@link AbstractFilter}s.
     */
    public BinaryOperationFilter(String operator, AbstractFilter... filters)
    {
        mFilters = filters;
        mOperator = operator;
    }


    @Override
    public final void getSelection(StringBuilder stringBuilder)
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
                stringBuilder.append(" (");
                stringBuilder.append(mOperator);
                stringBuilder.append(" (");
            }
            filter.getSelection(stringBuilder);
        }
        stringBuilder.append(")");
    }


    @Override
    public final void getSelectionArgs(List<String> selectionArgs)
    {
        for (AbstractFilter filter : mFilters)
        {
            filter.getSelectionArgs(selectionArgs);
        }
    }
}
