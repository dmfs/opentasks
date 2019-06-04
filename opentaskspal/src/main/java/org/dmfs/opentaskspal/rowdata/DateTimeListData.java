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

package org.dmfs.opentaskspal.rowdata;

import android.content.ContentProviderOperation;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.rfc5545.DateTime;


/**
 * {@link RowData} for a field of {@link DateTime}s.
 *
 * @param <Contract>
 *         The contract of the table this row data goes to.
 *
 * @author Marten Gajda
 */
public final class DateTimeListData<Contract> implements RowData<Contract>
{
    private final String mField;
    private final Iterable<DateTime> mDateTimes;


    public DateTimeListData(String field, @NonNull Iterable<DateTime> dateTimes)
    {
        mField = field;
        mDateTimes = dateTimes;
    }


    @NonNull
    @Override
    public ContentProviderOperation.Builder updatedBuilder(@NonNull TransactionContext transactionContext, @NonNull ContentProviderOperation.Builder builder)
    {
        String value = TextUtils.join(",",
                new Mapped<>(DateTime::toString, new Mapped<>(dt -> dt.isFloating() ? dt : dt.shiftTimeZone(DateTime.UTC), mDateTimes)));
        return builder.withValue(mField, value.isEmpty() ? null : value);
    }
}
