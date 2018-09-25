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

package org.dmfs.provider.tasks.model;

import android.content.ContentValues;
import android.database.Cursor;

import org.dmfs.provider.tasks.model.adapters.IntegerFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.LongFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.StringFieldAdapter;
import org.dmfs.tasks.contract.TaskContract.TaskLists;


/**
 * Adapter to read list values from primitive data sets like {@link Cursor}s or {@link ContentValues}s.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface ListAdapter extends EntityAdapter<ListAdapter>
{
    /**
     * Adapter for the row id of a task list.
     */
    LongFieldAdapter<ListAdapter> _ID = new LongFieldAdapter<ListAdapter>(TaskLists._ID);

    /**
     * Adapter for the _sync_id of a list.
     */
    StringFieldAdapter<ListAdapter> SYNC_ID = new StringFieldAdapter<ListAdapter>(TaskLists._SYNC_ID);

    /**
     * Adapter for the sync version of a list.
     */
    StringFieldAdapter<ListAdapter> SYNC_VERSION = new StringFieldAdapter<ListAdapter>(TaskLists.SYNC_VERSION);

    /**
     * Adapter for the account name of a list.
     */
    StringFieldAdapter<ListAdapter> ACCOUNT_NAME = new StringFieldAdapter<ListAdapter>(TaskLists.ACCOUNT_NAME);

    /**
     * Adapter for the account type of a list.
     */
    StringFieldAdapter<ListAdapter> ACCOUNT_TYPE = new StringFieldAdapter<ListAdapter>(TaskLists.ACCOUNT_TYPE);

    /**
     * Adapter for the owner of a list.
     */
    StringFieldAdapter<ListAdapter> OWNER = new StringFieldAdapter<ListAdapter>(TaskLists.OWNER);

    /**
     * Adapter for the name of a list.
     */
    StringFieldAdapter<ListAdapter> LIST_NAME = new StringFieldAdapter<ListAdapter>(TaskLists.LIST_NAME);

    /**
     * Adapter for the color of a list.
     */
    IntegerFieldAdapter<ListAdapter> LIST_COLOR = new IntegerFieldAdapter<ListAdapter>(TaskLists.LIST_COLOR);

    /***
     * Creates a {@link ListAdapter} for a new task initialized with the values of this task (except for _ID).
     *
     * @return A new task having the same values.
     */
    @Override
    ListAdapter duplicate();
}
