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

package org.dmfs.opentaskspal.contentpal.tables;

import android.content.ContentProviderClient;
import android.support.annotation.NonNull;

import org.dmfs.android.contentpal.InsertOperation;
import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.Predicate;
import org.dmfs.android.contentpal.Table;
import org.dmfs.android.contentpal.UriParams;
import org.dmfs.android.contentpal.View;
import org.dmfs.android.contentpal.predicates.AllOf;


/**
 * @author Gabor Keszthelyi
 */
public final class Filtered<T> implements Table<T>
{
    private final Predicate mPredicate;
    private final Table<T> mOriginal;


    public Filtered(Predicate predicate, Table<T> original)
    {
        mOriginal = original;
        mPredicate = predicate;
    }


    @NonNull
    @Override
    public InsertOperation<T> insertOperation(@NonNull UriParams uriParams)
    {
        return mOriginal.insertOperation(uriParams);
    }


    @NonNull
    @Override
    public Operation<T> updateOperation(@NonNull UriParams uriParams, @NonNull Predicate predicate)
    {
        return mOriginal.updateOperation(uriParams, new AllOf(predicate, mPredicate));
    }


    @NonNull
    @Override
    public Operation<T> deleteOperation(@NonNull UriParams uriParams, @NonNull Predicate predicate)
    {
        return mOriginal.deleteOperation(uriParams, new AllOf(predicate, mPredicate));
    }


    @NonNull
    @Override
    public Operation<T> assertOperation(@NonNull UriParams uriParams, @NonNull Predicate predicate)
    {
        return mOriginal.assertOperation(uriParams, new AllOf(predicate, mPredicate));
    }


    @NonNull
    @Override
    public View<T> view(@NonNull ContentProviderClient contentProviderClient, @NonNull String... projection)
    {
        return new org.dmfs.opentaskspal.contentpal.views.Filtered<>(mPredicate, mOriginal.view(contentProviderClient, projection));
    }
}
