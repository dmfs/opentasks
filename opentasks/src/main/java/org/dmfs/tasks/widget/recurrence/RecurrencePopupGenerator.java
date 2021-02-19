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
import org.dmfs.jems.iterable.elementary.Seq;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.procedure.Procedure;
import org.dmfs.jems.procedure.composite.ForEach;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;


public final class RecurrencePopupGenerator implements BiFunction<DateTime, Procedure<? super Optional<RecurrenceRule>>, Procedure<Menu>>
{
    private final Iterable<? extends BiFunction<? super DateTime, ? super Procedure<? super Optional<RecurrenceRule>>, ? extends Procedure<Menu>>> delegates;


    @SafeVarargs
    public RecurrencePopupGenerator(BiFunction<? super DateTime, ? super Procedure<? super Optional<RecurrenceRule>>, ? extends Procedure<Menu>>... delegates)
    {
        this(new Seq<>(delegates));
    }


    public RecurrencePopupGenerator(Iterable<? extends BiFunction<? super DateTime, ? super Procedure<? super Optional<RecurrenceRule>>, ? extends Procedure<Menu>>> delegates)
    {
        this.delegates = delegates;
    }


    @Override
    public Procedure<Menu> value(DateTime dateTime, Procedure<? super Optional<RecurrenceRule>> recurrenceRuleProcedure)
    {
        return menu -> new ForEach<>(delegates).process(d -> d.value(dateTime, recurrenceRuleProcedure).process(menu));
    }
}
