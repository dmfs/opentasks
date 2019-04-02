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

import android.content.ContentUris;
import android.net.Uri;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.opentaskspal.readdata.Id;
import org.dmfs.opentaskspal.readdata.TaskPin;
import org.dmfs.opentaskspal.readdata.TaskVersion;
import org.dmfs.tasks.contract.TaskContract;


/**
 * The {@link TaskNotificationState} of a {@link RowDataSnapshot} of a task.
 *
 * @author Marten Gajda
 */
public final class RowState implements TaskNotificationState
{
    private final String mAuthority;
    private final RowDataSnapshot<TaskContract.Tasks> mRow;


    public RowState(String authority, RowDataSnapshot<TaskContract.Tasks> row)
    {
        mAuthority = authority;
        mRow = row;
    }


    @Override
    public Uri task()
    {
        return ContentUris.withAppendedId(TaskContract.Tasks.getContentUri(mAuthority), new Id(mRow).value());
    }


    @Override
    public int taskVersion()
    {
        return new TaskVersion(mRow).value();
    }


    @Override
    public boolean ongoing()
    {
        return new TaskPin(mRow).value();
    }
}
