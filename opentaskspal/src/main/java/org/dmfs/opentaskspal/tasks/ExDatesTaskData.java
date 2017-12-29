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

package org.dmfs.opentaskspal.tasks;

import android.content.ContentProviderOperation;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link RowData} for tasks with EXDATEs
 * <p>
 * TODO: how to make sure this is only ever used with tasks having a start and/or due date?
 *
 * @author Marten Gajda
 */
public final class ExDatesTaskData implements RowData<TaskContract.Tasks>
{
    private final Iterable<DateTime> mExDates;


    public ExDatesTaskData(@NonNull Iterable<DateTime> exdates)
    {
        mExDates = exdates;
    }


    @NonNull
    @Override
    public ContentProviderOperation.Builder updatedBuilder(@NonNull TransactionContext transactionContext, @NonNull ContentProviderOperation.Builder builder)
    {
        String value = TextUtils.join(",",
                new Mapped<>(DateTime::toString, new Mapped<>(dt -> dt.isFloating() ? dt : dt.shiftTimeZone(DateTime.UTC), mExDates)));
        return builder.withValue(TaskContract.Tasks.EXDATE, value.isEmpty() ? null : value);
    }
}
