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

package org.dmfs.opentaskspal.tasks;

import androidx.annotation.NonNull;

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.rowdata.CharSequenceRowData;
import org.dmfs.android.contentpal.rowdata.DelegatingRowData;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link RowData} for {@link TaskContract.Tasks#_SYNC_ID}
 *
 * @author Gabor Keszthelyi
 */
public final class SyncIdData extends DelegatingRowData<TaskContract.Tasks>
{
    public SyncIdData(@NonNull CharSequence syncId)
    {
        // TODO CharSequenceRowData allows null so this class wouldn't fail with that but erase the value
        super(new CharSequenceRowData<TaskContract.Tasks>(TaskContract.Tasks._SYNC_ID, syncId));
    }
}
