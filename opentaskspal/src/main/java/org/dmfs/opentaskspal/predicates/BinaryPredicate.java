/*
 * Copyright 2018 dmfs GmbH
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

package org.dmfs.opentaskspal.predicates;

import android.support.annotation.NonNull;

import org.dmfs.android.contentpal.Predicate;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.jems.iterable.composite.Joined;
import org.dmfs.jems.iterable.decorators.Mapped;

import java.util.Iterator;


/**
 * A {@link Predicate} which connects a number of given predicates with the given binary operator. Like this:
 * <pre>{@code
 * (predicate_1) operator (predicate_2) operator (predicate_3) … operator (predicate_n)
 * }</pre>
 * <p>
 * If no predicates are given this always evaluates to "1".
 *
 * @author Marten Gajda
 * @deprecated This should be updated in ContentPal, see https://github.com/dmfs/ContentPal/issues/173
 */
@Deprecated
public final class BinaryPredicate implements Predicate
{
    private final Iterable<Predicate> mPredicates;
    private final String mOperator;


    public BinaryPredicate(@NonNull String operator, @NonNull Predicate... predicates)
    {
        this(operator, new Seq<>(predicates));
    }


    public BinaryPredicate(@NonNull String operator, @NonNull Iterable<Predicate> predicates)
    {
        mOperator = operator;
        mPredicates = predicates;
    }


    @NonNull
    @Override
    public CharSequence selection(@NonNull TransactionContext transactionContext)
    {
        Iterator<Predicate> iterator = mPredicates.iterator();
        if (!iterator.hasNext())
        {
            return "1";
        }
        StringBuilder result = new StringBuilder(256);
        result.append("( ");
        result.append(iterator.next().selection(transactionContext));
        while (iterator.hasNext())
        {
            result.append(" ) ");
            result.append(mOperator);
            result.append(" ( ");
            result.append(iterator.next().selection(transactionContext));
        }
        result.append(" )");
        return result;

    }


    @NonNull
    @Override
    public Iterable<Argument> arguments(@NonNull final TransactionContext transactionContext)
    {
        return new Joined<>(
                new Mapped<>(
                        predicate -> predicate.arguments(transactionContext),
                        mPredicates));
    }
}
