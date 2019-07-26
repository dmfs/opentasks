/*
 * Copyright 2018 dmfs GmbH
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

package org.dmfs.provider.tasks.processors;

import android.database.sqlite.SQLiteDatabase;

import org.dmfs.provider.tasks.model.EntityAdapter;


/**
 * A simple No-Op {@link EntityProcessor}.
 *
 * @author Marten Gajda
 */
public final class NoOpProcessor<T extends EntityAdapter<T>> implements EntityProcessor<T>
{
    @Override
    public T insert(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter)
    {
        return entityAdapter;
    }


    @Override
    public T update(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter)
    {
        return entityAdapter;
    }


    @Override
    public void delete(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter)
    {
        // do nothing
    }
}
