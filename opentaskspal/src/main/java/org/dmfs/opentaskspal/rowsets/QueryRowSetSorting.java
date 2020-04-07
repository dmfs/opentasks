/*
 * Copyright 2020 dmfs GmbH
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

package org.dmfs.opentaskspal.rowsets;


import android.database.Cursor;
import android.os.RemoteException;

import org.dmfs.android.contentpal.ClosableIterator;
import org.dmfs.android.contentpal.Predicate;
import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowSet;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.Table;
import org.dmfs.android.contentpal.View;
import org.dmfs.android.contentpal.rowdatasnapshots.MapRowDataSnapshot;
import org.dmfs.android.contentpal.rowsnapshots.ValuesRowSnapshot;
import org.dmfs.android.contentpal.tools.ClosableEmptyIterator;
import org.dmfs.android.contentpal.tools.uriparams.EmptyUriParams;
import org.dmfs.android.contentpal.transactions.contexts.EmptyTransactionContext;
import org.dmfs.iterators.AbstractBaseIterator;
import org.dmfs.jems.optional.Optional;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import androidx.annotation.NonNull;

import static org.dmfs.jems.optional.elementary.Absent.absent;


/**
 * A base {@link RowSet} which returns a {@link RowSnapshot} for each row of the given {@link Table} that matches the given {@link Predicate}.
 *
 * @author Marten Gajda
 */
public final class QueryRowSetSorting<T> implements RowSet<T>
{
    private final View<T> mView;
    private final Projection<? super T> mProjection;
    private final Predicate<? super T> mPredicate;
    private Optional<String> mSorting;


    public QueryRowSetSorting(@NonNull View<T> view, @NonNull Projection<? super T> projection, @NonNull Predicate<? super T> predicate, Optional<String> sorting)
    {
        mView = view;
        mProjection = projection;
        mPredicate = predicate;
        mSorting = sorting;
    }


    @NonNull
    @Override
    public ClosableIterator<RowSnapshot<T>> iterator()
    {
        try
        {
            Cursor cursor = mView.rows(EmptyUriParams.INSTANCE, mProjection, mPredicate,  mSorting);
            if (!cursor.moveToFirst())
            {
                cursor.close();
                return ClosableEmptyIterator.instance();
            }
            return new QueryRowSetSorting.RowIterator<>(cursor, mView.table());
        }
        catch (RemoteException e)
        {
            throw new RuntimeException(
                    String.format("Unable to execute query on view \"%s\" with selection \"%s\"",
                            mView.toString(),
                            mPredicate.selection(EmptyTransactionContext.INSTANCE).toString()), e);
        }
    }


    private final static class RowIterator<T> extends AbstractBaseIterator<RowSnapshot<T>> implements ClosableIterator<RowSnapshot<T>>
    {
        private final Cursor mCursor;
        private final Table<T> mTable;


        private RowIterator(@NonNull Cursor cursor, @NonNull Table<T> table)
        {
            mCursor = cursor;
            mTable = table;
        }


        @Override
        public boolean hasNext()
        {
            if (mCursor.isClosed())
            {
                return false;
            }
            try
            {
                if (mCursor.isAfterLast())
                {
                    mCursor.close();
                    return false;
                }
            }
            catch (Exception e)
            {
                // we can't use finally here, because we want to close the cursor only in an error case.
                mCursor.close();
                throw e;
            }

            return true;
        }


        @NonNull
        @Override
        public RowSnapshot<T> next()
        {
            try
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException("No more rows to iterate");
                }
                Map<String, String> charData = new HashMap<>();
                Map<String, byte[]> byteData = new HashMap<>();
                String[] columnNames = mCursor.getColumnNames();
                for (int i = 0, count = mCursor.getColumnCount(); i < count; ++i)
                {
                    String columnName = columnNames[i];
                    if (mCursor.getType(i) == Cursor.FIELD_TYPE_BLOB)
                    {
                        byteData.put(columnName, mCursor.getBlob(i));
                    }
                    else
                    {
                        charData.put(columnName, mCursor.getString(i));
                    }
                }
                RowSnapshot<T> rowSnapshot = new ValuesRowSnapshot<>(mTable, new MapRowDataSnapshot<>(charData, byteData));
                mCursor.moveToNext();
                return rowSnapshot;
            }
            catch (Exception e)
            {
                // we can't use finally here, because we want to close the cursor only in an error case.
                mCursor.close();
                throw e;
            }
        }


        @Override
        protected void finalize() throws Throwable
        {
            // Note this only serves as the very last resort. Normally the cursor is closed when the last item is iterated or when close() is called.
            // However if there is a crash during this the cursor might not be closed properly, hence we try that here.
            if (!mCursor.isClosed())
            {
                mCursor.close();
            }
            super.finalize();
        }


        @Override
        public void close()
        {
            if (!mCursor.isClosed())
            {
                mCursor.close();
            }
        }
    }

}
