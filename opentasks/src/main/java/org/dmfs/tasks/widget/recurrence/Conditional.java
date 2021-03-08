/*
 * Copyright 2021 dmfs GmbH
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

package org.dmfs.tasks.widget.recurrence;

import android.view.Menu;

import org.dmfs.jems.function.BiFunction;
import org.dmfs.jems.generator.Generator;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.predicate.Predicate;
import org.dmfs.jems.procedure.Procedure;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import androidx.annotation.NonNull;


public final class Conditional implements BiFunction<DateTime, Procedure<? super Optional<RecurrenceRule>>, Procedure<Menu>>
{
    private final Predicate<DateTime> mCondition;
    private final BiFunction<? super DateTime, ? super Procedure<? super Optional<RecurrenceRule>>, ? extends Procedure<Menu>> mDelegate;
    private final Generator<Procedure<Menu>> mDefaultGenerator;


    public Conditional(
            @NonNull Predicate<DateTime> condition,
            @NonNull BiFunction<? super DateTime, ? super Procedure<? super Optional<RecurrenceRule>>, ? extends Procedure<Menu>> delegate)
    {
        this(condition, delegate, () -> ignored -> {
        });
    }


    public Conditional(
            @NonNull Predicate<DateTime> condition,
            @NonNull BiFunction<? super DateTime, ? super Procedure<? super Optional<RecurrenceRule>>, ? extends Procedure<Menu>> delegate,
            @NonNull Generator<Procedure<Menu>> defaultGenerator)
    {
        mCondition = condition;
        mDelegate = delegate;
        mDefaultGenerator = defaultGenerator;
    }


    @Override
    public Procedure<Menu> value(DateTime dateTime, Procedure<? super Optional<RecurrenceRule>> recurrenceRuleProcedure)
    {
        if (mCondition.satisfiedBy(dateTime))
        {
            return mDelegate.value(dateTime, recurrenceRuleProcedure);
        }
        return mDefaultGenerator.next();
    }
}
