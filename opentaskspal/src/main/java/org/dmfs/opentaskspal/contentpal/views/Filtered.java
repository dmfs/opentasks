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

package org.dmfs.opentaskspal.contentpal.views;

import android.database.Cursor;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import org.dmfs.android.contentpal.Predicate;
import org.dmfs.android.contentpal.Table;
import org.dmfs.android.contentpal.UriParams;
import org.dmfs.android.contentpal.View;
import org.dmfs.android.contentpal.predicates.AllOf;
import org.dmfs.optional.Optional;


/**
 * @author Gabor Keszthelyi
 */
public final class Filtered<T> implements View<T>
{
    private final Predicate mPredicate;
    private final View<T> mOriginal;


    public Filtered(Predicate predicate, View<T> original)
    {
        mPredicate = predicate;
        mOriginal = original;
    }


    @NonNull
    @Override
    public Cursor rows(@NonNull UriParams uriParams, @NonNull Predicate predicate, @NonNull Optional<String> sorting) throws RemoteException
    {
        return mOriginal.rows(uriParams, new AllOf(predicate, mPredicate), sorting);
    }


    @NonNull
    @Override
    public Table<T> table()
    {
        return new org.dmfs.opentaskspal.contentpal.tables.Filtered<>(mPredicate, mOriginal.table());
    }


    @NonNull
    @Override
    public View<T> withProjection(@NonNull String... projection)
    {
        return mOriginal.withProjection(projection);
    }
}
