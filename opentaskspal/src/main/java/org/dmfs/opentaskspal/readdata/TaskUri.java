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

package org.dmfs.opentaskspal.readdata;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.jems.single.Single;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Tasks;

import androidx.annotation.NonNull;


/**
 * {@link Single} for the content {@link Uri} that refers to the given {@link RowDataSnapshot}.
 *
 * @author Gabor Keszthelyi
 */
public final class TaskUri implements Single<Uri>
{
    public static final Projection<? super BaseColumns> PROJECTION = Id.PROJECTION;

    private final RowDataSnapshot<? extends TaskContract.TaskColumns> mRowDataSnapshot;
    private final String mAuthority;


    public TaskUri(@NonNull String authority, @NonNull RowDataSnapshot<? extends TaskContract.TaskColumns> rowDataSnapshot)
    {
        mAuthority = authority;
        mRowDataSnapshot = rowDataSnapshot;
    }


    @Override
    public Uri value()
    {
        // TODO: use the instance URI one we support recurrence
        return ContentUris.withAppendedId(Tasks.getContentUri(mAuthority), new Id(mRowDataSnapshot).value());
    }
}
