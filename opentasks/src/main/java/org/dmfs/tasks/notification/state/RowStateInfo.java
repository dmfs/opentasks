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

package org.dmfs.tasks.notification.state;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.opentaskspal.readdata.EffectiveDueDate;
import org.dmfs.opentaskspal.readdata.TaskIsClosed;
import org.dmfs.opentaskspal.readdata.TaskPin;
import org.dmfs.opentaskspal.readdata.TaskStart;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;

import androidx.annotation.NonNull;


/**
 * The {@link StateInfo} of a {@link RowDataSnapshot}.
 *
 * @author Marten Gajda
 */
public final class RowStateInfo implements StateInfo
{
    private final RowDataSnapshot<? extends TaskContract.Instances> mRow;


    public RowStateInfo(@NonNull RowDataSnapshot<? extends TaskContract.Instances> row)
    {
        mRow = row;
    }


    @Override
    public boolean pinned()
    {
        return new TaskPin(mRow).value();
    }


    @Override
    public boolean due()
    {
        return new Backed<>(new Mapped<>(this::isPast, new EffectiveDueDate(mRow)), false).value();
    }


    @Override
    public boolean started()
    {
        return new Backed<>(new Mapped<>(this::isPast, new TaskStart(mRow)), false).value();
    }


    @Override
    public boolean done()
    {
        return new TaskIsClosed(mRow).value();
    }


    private boolean isPast(@NonNull DateTime dt)
    {
        DateTime now = DateTime.nowAndHere();
        dt = dt.isAllDay() ? dt.startOfDay() : dt;
        dt = dt.isFloating() ? dt.swapTimeZone(now.getTimeZone()) : dt;
        return !now.before(dt);
    }
}
