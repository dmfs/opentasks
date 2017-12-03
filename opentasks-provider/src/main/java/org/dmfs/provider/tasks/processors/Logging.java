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

package org.dmfs.provider.tasks.processors;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.dmfs.provider.tasks.model.EntityAdapter;


/**
 * @author Marten Gajda
 */
public final class Logging<T extends EntityAdapter<T>> implements EntityProcessor<T>
{
    public static final String TAG = "Logging EntityProcessor";
    private final EntityProcessor<T> mDelegate;


    public Logging(EntityProcessor<T> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public T insert(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter)
    {
        Log.d(TAG, "before insert");
        T result = mDelegate.insert(db, entityAdapter, isSyncAdapter);
        Log.d(TAG, "after insert on " + entityAdapter.id());
        return result;
    }


    @Override
    public T update(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter)
    {
        Log.d(TAG, "before update of " + entityAdapter.id());
        T result = mDelegate.update(db, entityAdapter, isSyncAdapter);
        Log.d(TAG, "after update of " + entityAdapter.id());
        return result;
    }


    @Override
    public void delete(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter)
    {
        Log.d(TAG, "before delete of " + entityAdapter.id());
        mDelegate.delete(db, entityAdapter, isSyncAdapter);
        Log.d(TAG, "after delete of " + entityAdapter.id());
    }
}
