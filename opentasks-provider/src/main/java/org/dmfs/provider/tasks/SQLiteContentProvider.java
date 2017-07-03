/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.dmfs.provider.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;


/**
 * General purpose {@link ContentProvider} base class that uses SQLiteDatabase for storage.
 */
/*
 * Changed by marten@dmfs.org:
 * 
 * removed protected mDb field and replaced it by local fields. There is no reason to store the database if we get a new one for every transaction. Instead we
 * also pass the database to the *InTransaction methods.
 * 
 * update visibility of class and methods
 */
abstract class SQLiteContentProvider extends ContentProvider
{

	@SuppressWarnings("unused")
	private static final String TAG = "SQLiteContentProvider";

	private SQLiteOpenHelper mOpenHelper;
	private Set<Uri> mChangedUris;

	private final ThreadLocal<Boolean> mApplyingBatch = new ThreadLocal<Boolean>();
	private static final int SLEEP_AFTER_YIELD_DELAY = 4000;

	/**
	 * Maximum number of operations allowed in a batch between yield points.
	 */
	private static final int MAX_OPERATIONS_PER_YIELD_POINT = 500;


	@Override
	public boolean onCreate()
	{
		Context context = getContext();
		mOpenHelper = getDatabaseHelper(context);
		mChangedUris = new HashSet<Uri>();
		return true;
	}


	/**
	 * Returns a {@link SQLiteOpenHelper} that can open the database.
	 */
	protected abstract SQLiteOpenHelper getDatabaseHelper(Context context);


	/**
	 * The equivalent of the {@link #insert} method, but invoked within a transaction.
	 */
	public abstract Uri insertInTransaction(SQLiteDatabase db, Uri uri, ContentValues values, boolean callerIsSyncAdapter);


	/**
	 * The equivalent of the {@link #update} method, but invoked within a transaction.
	 */
	public abstract int updateInTransaction(SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs,
		boolean callerIsSyncAdapter);


	/**
	 * The equivalent of the {@link #delete} method, but invoked within a transaction.
	 */
	public abstract int deleteInTransaction(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs, boolean callerIsSyncAdapter);


	/**
	 * Call this to add a URI to the list of URIs to be notified when the transaction is committed.
	 */
	protected void postNotifyUri(Uri uri)
	{
		synchronized (mChangedUris)
		{
			mChangedUris.add(uri);
		}
	}


	public boolean isCallerSyncAdapter(Uri uri)
	{
		return false;
	}


	public SQLiteOpenHelper getDatabaseHelper()
	{
		return mOpenHelper;
	}


	private boolean applyingBatch()
	{
		return mApplyingBatch.get() != null && mApplyingBatch.get();
	}


	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		Uri result = null;
		boolean callerIsSyncAdapter = isCallerSyncAdapter(uri);
		boolean applyingBatch = applyingBatch();
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		if (!applyingBatch)
		{
			db.beginTransaction();
			try
			{
				result = insertInTransaction(db, uri, values, callerIsSyncAdapter);
				db.setTransactionSuccessful();
			}
			finally
			{
				db.endTransaction();
			}

			onEndTransaction(callerIsSyncAdapter);
		}
		else
		{
			result = insertInTransaction(db, uri, values, callerIsSyncAdapter);
		}
		return result;
	}


	@Override
	public int bulkInsert(Uri uri, ContentValues[] values)
	{
		int numValues = values.length;
		boolean callerIsSyncAdapter = isCallerSyncAdapter(uri);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try
		{
			for (int i = 0; i < numValues; i++)
			{
				insertInTransaction(db, uri, values[i], callerIsSyncAdapter);
				db.yieldIfContendedSafely();
			}
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}

		onEndTransaction(callerIsSyncAdapter);
		return numValues;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		int count = 0;
		boolean callerIsSyncAdapter = isCallerSyncAdapter(uri);
		boolean applyingBatch = applyingBatch();
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		if (!applyingBatch)
		{
			db.beginTransaction();
			try
			{
				count = updateInTransaction(db, uri, values, selection, selectionArgs, callerIsSyncAdapter);
				db.setTransactionSuccessful();
			}
			finally
			{
				db.endTransaction();
			}

			onEndTransaction(callerIsSyncAdapter);
		}
		else
		{
			count = updateInTransaction(db, uri, values, selection, selectionArgs, callerIsSyncAdapter);
		}

		return count;
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		int count = 0;
		boolean callerIsSyncAdapter = isCallerSyncAdapter(uri);
		boolean applyingBatch = applyingBatch();
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		if (!applyingBatch)
		{
			db.beginTransaction();
			try
			{
				count = deleteInTransaction(db, uri, selection, selectionArgs, callerIsSyncAdapter);
				db.setTransactionSuccessful();
			}
			finally
			{
				db.endTransaction();
			}

			onEndTransaction(callerIsSyncAdapter);
		}
		else
		{
			count = deleteInTransaction(db, uri, selection, selectionArgs, callerIsSyncAdapter);
		}
		return count;
	}


	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException
	{
		int ypCount = 0;
		int opCount = 0;
		boolean callerIsSyncAdapter = false;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try
		{
			mApplyingBatch.set(true);
			final int numOperations = operations.size();
			final ContentProviderResult[] results = new ContentProviderResult[numOperations];
			for (int i = 0; i < numOperations; i++)
			{
				if (++opCount >= MAX_OPERATIONS_PER_YIELD_POINT)
				{
					throw new OperationApplicationException("Too many content provider operations between yield points. "
						+ "The maximum number of operations per yield point is " + MAX_OPERATIONS_PER_YIELD_POINT, ypCount);
				}
				final ContentProviderOperation operation = operations.get(i);
				if (!callerIsSyncAdapter && isCallerSyncAdapter(operation.getUri()))
				{
					callerIsSyncAdapter = true;
				}
				if (i > 0 && operation.isYieldAllowed())
				{
					opCount = 0;
					if (db.yieldIfContendedSafely(SLEEP_AFTER_YIELD_DELAY))
					{
						ypCount++;
					}
				}
				results[i] = operation.apply(this, results, i);
			}
			db.setTransactionSuccessful();
			return results;
		}
		finally
		{
			mApplyingBatch.set(false);
			db.endTransaction();
			onEndTransaction(callerIsSyncAdapter);
		}
	}


	protected void onEndTransaction(boolean callerIsSyncAdapter)
	{
		Set<Uri> changed;
		synchronized (mChangedUris)
		{
			changed = new HashSet<Uri>(mChangedUris);
			mChangedUris.clear();
		}
		ContentResolver resolver = getContext().getContentResolver();
		for (Uri uri : changed)
		{
			boolean syncToNetwork = !callerIsSyncAdapter && syncToNetwork(uri);
			resolver.notifyChange(uri, null, syncToNetwork);
		}
	}


	protected boolean syncToNetwork(Uri uri)
	{
		return false;
	}

}
