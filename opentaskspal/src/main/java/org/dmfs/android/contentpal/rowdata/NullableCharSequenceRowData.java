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

package org.dmfs.android.contentpal.rowdata;

import android.content.ContentProviderOperation;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.TransactionContext;


/**
 * {@link RowData} for a key and a @{@link Nullable} {@link CharSequence} value, {@link Object#toString} is used in case the value is non-null.
 *
 * @author Gabor Keszthelyi
 */
// TODO Use from contentpal when available
public final class NullableCharSequenceRowData<T> implements RowData<T>
{
    private final String mKey;
    private final CharSequence mValue;


    public NullableCharSequenceRowData(@NonNull String key, @Nullable CharSequence value)
    {
        mKey = key;
        mValue = value;
    }


    @NonNull
    @Override
    public ContentProviderOperation.Builder updatedBuilder(TransactionContext transactionContext, @NonNull ContentProviderOperation.Builder builder)
    {
        return builder.withValue(mKey, mValue == null ? null : mValue.toString());
    }
}
