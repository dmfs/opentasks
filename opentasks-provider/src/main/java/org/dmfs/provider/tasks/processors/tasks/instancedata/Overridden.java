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

import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.jems.procedure.composite.ForEach;
import org.dmfs.jems.single.Single;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A decorator for {@link Single}s of Instance {@link ContentValues} which populates the {@link TaskContract.Instances#INSTANCE_ORIGINAL_TIME} field based on
 * the given {@link Optional} original start.
 *
 * @author Marten Gajda
 */
public final class Overridden implements Single<ContentValues>
{
    private final Optional<DateTime> mOriginalTime;
    private final Single<ContentValues> mDelegate;


    public Overridden(DateTime originalTime, ContentValues delegate)
    {
        this(new Present<>(originalTime), () -> delegate);
    }


    public Overridden(Optional<DateTime> originalTime, Single<ContentValues> delegate)
    {
        mOriginalTime = originalTime;
        mDelegate = delegate;
    }


    @Override
    public ContentValues value()
    {
        ContentValues values = mDelegate.value();
        new ForEach<>(new Mapped<>(DateTime::getTimestamp, mOriginalTime)).process(time -> values.put(TaskContract.Instances.INSTANCE_ORIGINAL_TIME, time));
        return values;
    }
}
