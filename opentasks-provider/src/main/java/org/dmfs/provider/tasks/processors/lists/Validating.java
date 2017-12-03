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

package org.dmfs.provider.tasks.processors.lists;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.dmfs.provider.tasks.model.ListAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;


/**
 * A processor to validate the values of a task list.
 *
 * @author Marten Gajda
 */
public final class Validating implements EntityProcessor<ListAdapter>
{
    private final EntityProcessor<ListAdapter> mDelegate;


    public Validating(EntityProcessor<ListAdapter> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public ListAdapter insert(SQLiteDatabase db, ListAdapter list, boolean isSyncAdapter)
    {
        if (!isSyncAdapter)
        {
            throw new UnsupportedOperationException("Caller must be a sync adapter to create task lists");
        }

        if (TextUtils.isEmpty(list.valueOf(ListAdapter.ACCOUNT_NAME)))
        {
            throw new IllegalArgumentException("ACCOUNT_NAME is required on INSERT");
        }

        if (TextUtils.isEmpty(list.valueOf(ListAdapter.ACCOUNT_TYPE)))
        {
            throw new IllegalArgumentException("ACCOUNT_TYPE is required on INSERT");
        }

        verifyCommon(list, isSyncAdapter);
        return mDelegate.insert(db, list, isSyncAdapter);
    }


    @Override
    public ListAdapter update(SQLiteDatabase db, ListAdapter list, boolean isSyncAdapter)
    {
        if (list.isUpdated(ListAdapter.ACCOUNT_NAME))
        {
            throw new IllegalArgumentException("ACCOUNT_NAME is write-once");
        }

        if (list.isUpdated(ListAdapter.ACCOUNT_TYPE))
        {
            throw new IllegalArgumentException("ACCOUNT_TYPE is write-once");
        }

        verifyCommon(list, isSyncAdapter);
        return mDelegate.update(db, list, isSyncAdapter);
    }


    @Override
    public void delete(SQLiteDatabase db, ListAdapter entityAdapter, boolean isSyncAdapter)
    {
        if (!isSyncAdapter)
        {
            throw new UnsupportedOperationException("Caller must be a sync adapter to delete task lists");
        }
        mDelegate.delete(db, entityAdapter, isSyncAdapter);
    }


    /**
     * Performs tests that are common to insert an update operations.
     *
     * @param list
     *         The {@link ListAdapter} to verify.
     * @param isSyncAdapter
     *         <code>true</code> if the caller is a sync adapter, false otherwise.
     */
    private void verifyCommon(ListAdapter list, boolean isSyncAdapter)
    {
        // row id can not be changed or set manually
        if (list.isUpdated(ListAdapter._ID))
        {
            throw new IllegalArgumentException("_ID can not be set manually");
        }

        if (isSyncAdapter)
        {
            // sync adapters may do all the stuff below
            return;
        }

        if (list.isUpdated(ListAdapter.LIST_COLOR))
        {
            throw new IllegalArgumentException("Only sync adapters can change the LIST_COLOR.");
        }
        if (list.isUpdated(ListAdapter.LIST_NAME))
        {
            throw new IllegalArgumentException("Only sync adapters can change the LIST_NAME.");
        }
        if (list.isUpdated(ListAdapter.SYNC_ID))
        {
            throw new IllegalArgumentException("Only sync adapters can change the _SYNC_ID.");
        }
        if (list.isUpdated(ListAdapter.SYNC_VERSION))
        {
            throw new IllegalArgumentException("Only sync adapters can change SYNC_VERSION.");
        }
        if (list.isUpdated(ListAdapter.OWNER))
        {
            throw new IllegalArgumentException("Only sync adapters can change the list OWNER.");
        }
    }
}
