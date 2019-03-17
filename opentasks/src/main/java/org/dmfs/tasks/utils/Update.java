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

import android.content.ContentProviderOperation;
import android.support.annotation.NonNull;

import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.RowReference;
import org.dmfs.android.contentpal.SoftRowReference;
import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.jems.optional.Optional;

import static org.dmfs.jems.optional.elementary.Absent.absent;


/**
 * An update {@link Operation} on a database row identified by its {@link RowReference}.
 * <p>
 * TODO: move to contentpal
 *
 * @author Marten Gajda
 */
@Deprecated
public final class Update<T> implements Operation<T>
{
    private final RowReference<T> mReference;
    private final RowData<T> mData;


    public Update(RowReference<T> reference, RowData<T> data)
    {
        mReference = reference;
        mData = data;
    }


    @NonNull
    @Override
    public Optional<SoftRowReference<T>> reference()
    {
        return absent();
    }


    @NonNull
    @Override
    public ContentProviderOperation.Builder contentOperationBuilder(@NonNull TransactionContext transactionContext) throws UnsupportedOperationException
    {
        return mData.updatedBuilder(transactionContext, mReference.putOperationBuilder(transactionContext));
    }
}
