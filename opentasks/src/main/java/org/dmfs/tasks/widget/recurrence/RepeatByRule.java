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
import org.dmfs.jems.function.Function;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.jems.procedure.Procedure;
import org.dmfs.jems.single.Single;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import androidx.annotation.NonNull;


public final class RepeatByRule implements BiFunction<DateTime, Procedure<? super Optional<RecurrenceRule>>, Procedure<Menu>>
{
    private final Function<Optional<RecurrenceRule>, String> mRruleToStringFunction;
    private final Single<RecurrenceRule> mRuleSingle;


    public RepeatByRule(Function<Optional<RecurrenceRule>, String> rruleToStringFunction, @NonNull Single<RecurrenceRule> ruleSingle)
    {
        mRruleToStringFunction = rruleToStringFunction;
        mRuleSingle = ruleSingle;
    }


    @Override
    public Procedure<Menu> value(DateTime dateTime, Procedure<? super Optional<RecurrenceRule>> recurrenceRuleProcedure)
    {
        Optional<RecurrenceRule> rrule = new Present<>(mRuleSingle.value());
        return menu -> menu.add(mRruleToStringFunction.value(rrule))
                .setOnMenuItemClickListener(item -> {
                    recurrenceRuleProcedure.process(rrule);
                    return true;
                });
    }
}
