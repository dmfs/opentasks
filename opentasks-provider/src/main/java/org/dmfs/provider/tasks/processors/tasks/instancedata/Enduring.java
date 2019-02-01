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

package org.dmfs.provider.tasks.processors.tasks.instancedata;

import android.content.ContentValues;

import org.dmfs.jems.optional.composite.Zipped;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A decorator for {@link Single}s of Instance {@link ContentValues} which populates the {@link TaskContract.Instances#INSTANCE_DURATION} field based on the
 * already populated {@link TaskContract.Instances#INSTANCE_START} and {@link TaskContract.Instances#INSTANCE_DUE} fields.
 *
 * @author Marten Gajda
 */
public final class Enduring implements Single<ContentValues>
{
    private final Single<ContentValues> mDelegate;


    public Enduring(Single<ContentValues> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public ContentValues value()
    {
        ContentValues values = mDelegate.value();
        // just store the difference between due and start, if both are present, otherwise store null
        values.put(TaskContract.Instances.INSTANCE_DURATION,
                new Backed<Long>(
                        new Zipped<>(
                                new NullSafe<>(values.getAsLong(TaskContract.Instances.INSTANCE_START)),
                                new NullSafe<>(values.getAsLong(TaskContract.Instances.INSTANCE_DUE)),
                                (start, due) -> due - start),
                        () -> null).value());
        return values;
    }
}
