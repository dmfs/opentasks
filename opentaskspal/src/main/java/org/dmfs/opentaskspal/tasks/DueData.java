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

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;

import androidx.annotation.NonNull;


/**
 * {@link RowData} for setting only the due datetime of the task, for setting start as well use {@link TimeData}.
 *
 * @author Gabor Keszthelyi
 */
public final class DueData<T extends TaskContract.TaskColumns> implements RowData<T>
{
    private final DateTime mDue;


    public DueData(@NonNull DateTime due)
    {
        mDue = due;
    }


    @NonNull
    @Override
    public ContentProviderOperation.Builder updatedBuilder(@NonNull TransactionContext transactionContext, @NonNull ContentProviderOperation.Builder builder)
    {
        return builder
                .withValue(TaskContract.Tasks.DUE, mDue.getTimestamp())
                .withValue(TaskContract.Tasks.TZ, mDue.isAllDay() ? "UTC" : mDue.getTimeZone().getID())
                .withValue(TaskContract.Tasks.IS_ALLDAY, mDue.isAllDay() ? 1 : 0)
                .withValue(TaskContract.Tasks.DTSTART, null)
                .withValue(TaskContract.Tasks.DURATION, null);
    }

}
