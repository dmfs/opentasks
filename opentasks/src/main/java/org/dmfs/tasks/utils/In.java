/*
 * Copyright 2019 dmfs GmbH
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

package org.dmfs.tasks.utils;

import androidx.annotation.NonNull;

import org.dmfs.android.contentpal.Predicate;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.android.contentpal.predicates.arguments.ValueArgument;
import org.dmfs.jems.iterable.decorators.Mapped;


/**
 * The conditional {@code IN} operator. Validates to {@code true} if a given column has any of the given values (their string representation to be more
 * precise).
 * <p>
 * Note, {@code null} is not a supported value.
 * <p>
 * TODO: move to ContentPal
 *
 * @author Marten Gajda
 */
@Deprecated
public final class In implements Predicate
{
    private final String mColumnName;
    private final Iterable<?> mArguments;


    public In(@NonNull String columnName, @NonNull Iterable<?> arguments)
    {
        mColumnName = columnName;
        mArguments = arguments;
    }


    @NonNull
    @Override
    public CharSequence selection(@NonNull TransactionContext transactionContext)
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(mColumnName);
        sb.append(" in (");
        boolean first = true;
        for (Object arg : mArguments)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                sb.append(", ");
            }
            sb.append("?");
        }
        sb.append(" ) ");
        return sb;
    }


    @NonNull
    @Override
    public Iterable<Argument> arguments(@NonNull TransactionContext transactionContext)
    {
        return new Mapped<>(ValueArgument::new, mArguments);
    }
}
