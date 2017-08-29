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

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.TransactionContext;


/**
 * Base class for {@link RowData}s that simply delegate to another {@link RowData}, possibly composed in the constructor.
 *
 * @author Gabor Keszthelyi
 */
// TODO Use from contentpal when available
public abstract class DelegatingRowData<T> implements RowData<T>
{
    private final RowData<T> mDelegate;


    public DelegatingRowData(RowData<T> delegate)
    {
        mDelegate = delegate;
    }


    @NonNull
    @Override
    public final ContentProviderOperation.Builder updatedBuilder(TransactionContext transactionContext, @NonNull ContentProviderOperation.Builder builder)
    {
        return mDelegate.updatedBuilder(transactionContext, builder);
    }
}
