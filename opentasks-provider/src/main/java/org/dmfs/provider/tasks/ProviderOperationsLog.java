/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
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
 * 
 */

package org.dmfs.provider.tasks;

import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;


/**
 * A log to track all content provider operations.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ProviderOperationsLog
{
	private ArrayList<Uri> mUris = new ArrayList<Uri>(16);

	private ArrayList<Integer> mOperations = new ArrayList<Integer>(16);


	/**
	 * Add an operation on the given {@link Uri} to the log.
	 * 
	 * @param operation
	 *            The {@link ProviderOperation} that was executed.
	 * @param uri
	 *            The {@link Uri} that the operation was executed on.
	 */
	public void log(ProviderOperation operation, Uri uri)
	{
		synchronized (this)
		{
			mUris.add(uri);
			mOperations.add(operation.ordinal());
		}
	}


	/**
	 * Adds the operations log to the given {@link Bundle}, creating one if the given bundle is <code>null</code>.
	 * 
	 * @param bundle
	 *            A {@link Bundle} or <code>null</code>.
	 * @param clearLog
	 *            <code>true</code> to clear the log afterwards, <code>false</code> to keep it.
	 * @return The {@link Bundle} that was passed or created.
	 */
	public Bundle toBundle(Bundle bundle, boolean clearLog)
	{
		if (bundle == null)
		{
			bundle = new Bundle(2);
		}

		synchronized (this)
		{
			bundle.putParcelableArrayList(TaskContract.EXTRA_OPERATIONS_URIS, mUris);
			bundle.putIntegerArrayList(TaskContract.EXTRA_OPERATIONS, mOperations);
			if (clearLog)
			{
				// we can't just clear the ArrayLists, because the Bundle keeps a reference to them
				mUris = new ArrayList<Uri>(16);
				mOperations = new ArrayList<Integer>(16);
			}
		}
		return bundle;
	}


	/**
	 * Returns a new {@link Bundle} containing the log.
	 * 
	 * @param clearLog
	 *            <code>true</code> to clear the log afterwards, <code>false</code> to keep it.
	 * @return The {@link Bundle} that was created.
	 */
	public Bundle toBundle(boolean clearLog)
	{
		return toBundle(null, clearLog);
	}


	/**
	 * Returns whether any operations have been logged or not.
	 * 
	 * @return <code>true</code> if this log is empty, <code>false</code> if it contains any logs of operations.
	 */
	public boolean isEmpty()
	{
		return mUris.size() == 0;
	}
}