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

package org.dmfs.tasks.readdata;

import android.content.Context;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.RowSet;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.rowsets.Frozen;
import org.dmfs.iterables.decorators.Mapped;
import org.dmfs.tasks.utils.rxjava.DelegatingSingle;

import io.reactivex.Single;


/**
 * {@link Single} that accesses the Tasks provider, runs the given {@link CpQuery}
 * and delivers the the result {@link Iterable} of {@link RowDataSnapshot}s.
 *
 * @author Gabor Keszthelyi
 */
public final class CpQuerySource<T> extends DelegatingSingle<Iterable<RowDataSnapshot<T>>>
{

    public CpQuerySource(Context context, CpQuery<T> cpQuery)
    {
        super(new ContentProviderClientSource(context)
                .map(client ->
                {
                    RowSet<T> frozen = new Frozen<>(cpQuery.rowSet(client, context));
                    frozen.iterator(); // To actually freeze it

                    return new Mapped<>(frozen, RowSnapshot::values);
                }));
    }

}
