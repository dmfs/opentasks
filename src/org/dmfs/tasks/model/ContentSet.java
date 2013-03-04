/*
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.dmfs.tasks.utils.AsyncContentLoader;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnContentLoadedListener;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * A ContentSet takes care of loading and storing the values for a specific {@link Uri}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ContentSet implements OnContentLoadedListener, Parcelable
{
	private ContentValues mBeforeContentValues;
	private ContentValues mAfterContentValues;
	private Uri mUri;
	private final Map<String, Set<OnContentChangeListener>> mOnChangeListeners = new HashMap<String, Set<OnContentChangeListener>>();


	/**
	 * Private constructor to read from a parcel;
	 */
	private ContentSet()
	{
	}


	public ContentSet(Uri uri)
	{
		if (uri == null)
		{
			throw new IllegalArgumentException("uri must not be null");
		}

		mUri = uri;
	}


	public void update(Context context, ContentValueMapper mapper)
	{
		if (!isInsert())
		{
			new AsyncContentLoader(context, this, mapper).execute(mUri);
		}
	}


	@Override
	public void onContentLoaded(ContentValues values)
	{
		mBeforeContentValues = values;
		notifyListeners(null);
	}


	public void delete(Context context)
	{
		if (mBeforeContentValues != null)
		{
			context.getContentResolver().delete(mUri, null, null);
			mBeforeContentValues = null;
			mAfterContentValues = null;
		}
	}


	public Uri persist(Context context)
	{
		if (mAfterContentValues == null || mAfterContentValues.size() == 0)
		{
			// nothing to do here
			return mUri;
		}

		if (isInsert())
		{
			// update uri with new uri
			mUri = context.getContentResolver().insert(mUri, mAfterContentValues);
		}
		else if (isUpdate())
		{
			context.getContentResolver().update(mUri, mAfterContentValues, null, null);
		}
		// else nothing to do

		return mUri;
	}


	public boolean isInsert()
	{
		return mBeforeContentValues == null && mAfterContentValues != null && mAfterContentValues.size() > 0;
	}


	public boolean isUpdate()
	{
		return mBeforeContentValues != null && mAfterContentValues != null && mAfterContentValues.size() > 0;
	}


	public boolean containsKey(String key)
	{
		return mAfterContentValues != null && mAfterContentValues.containsKey(key) || mBeforeContentValues != null && mBeforeContentValues.containsKey(key);
	}


	public boolean persistsKey(String key)
	{
		return mAfterContentValues != null && mAfterContentValues.containsKey(key);
	}


	public boolean containsAnyKey(Set<String> keys)
	{
		if (mAfterContentValues == null)
		{
			return false;
		}
		Set<String> keySet = mAfterContentValues.keySet();

		int sizeBefore = keySet.size();
		keySet.removeAll(keys);

		// if the number of keys has changed there was at least one common key in both sets
		return keySet.size() != sizeBefore;
	}


	public void ensureValues(Set<String> keys)
	{
		if (mBeforeContentValues == null)
		{
			// nothing to do
			return;
		}

		// make a copy of mBeforeContentValues
		ContentValues tempValues = new ContentValues(mBeforeContentValues);

		// remove all keys we don't preserver
		for (String key : tempValues.keySet())
		{
			if (!keys.contains(key))
			{
				tempValues.remove(key);
			}
		}

		// add values to mAfterContentValues
		if (mAfterContentValues != null)
		{
			mAfterContentValues.putAll(tempValues);
		}
		else
		{
			mAfterContentValues = tempValues;
		}
	}


	private ContentValues ensureAfter()
	{
		ContentValues values = mAfterContentValues;
		if (values == null)
		{
			values = new ContentValues();
			mAfterContentValues = values;
		}
		return values;
	}


	public void put(String key, Integer value)
	{
		ensureAfter().put(key, value);
		notifyListeners(key);
	}


	public Integer getAsInteger(String key)
	{
		final ContentValues after = mAfterContentValues;
		if (after != null && after.containsKey(key))
		{
			return mAfterContentValues.getAsInteger(key);
		}
		return mBeforeContentValues == null ? null : mBeforeContentValues.getAsInteger(key);
	}


	public void put(String key, Long value)
	{
		ensureAfter().put(key, value);
		notifyListeners(key);
	}


	public Long getAsLong(String key)
	{
		final ContentValues after = mAfterContentValues;
		if (after != null && after.containsKey(key))
		{
			return mAfterContentValues.getAsLong(key);
		}
		return mBeforeContentValues == null ? null : mBeforeContentValues.getAsLong(key);
	}


	public void put(String key, String value)
	{
		ensureAfter().put(key, value);
		notifyListeners(key);
	}


	public String getAsString(String key)
	{
		final ContentValues after = mAfterContentValues;
		if (after != null && after.containsKey(key))
		{
			return mAfterContentValues.getAsString(key);
		}
		return mBeforeContentValues == null ? null : mBeforeContentValues.getAsString(key);
	}


	public void remove(String key)
	{
		if (mAfterContentValues != null)
		{
			mAfterContentValues.remove(key);
		}
	}


	public void addOnChangeListener(OnContentChangeListener listener, String key, boolean notify)
	{
		Set<OnContentChangeListener> listenerSet = mOnChangeListeners.get(key);
		if (listenerSet == null)
		{
			// using a "WeakHashSet" ensures that we don't prevent listeners from getting garbage-collected.
			listenerSet = Collections.newSetFromMap(new WeakHashMap<OnContentChangeListener, Boolean>());
			mOnChangeListeners.put(key, listenerSet);
		}

		listenerSet.add(listener);

		if (notify && (mBeforeContentValues != null || mAfterContentValues != null))
		{
			listener.onContentChanged(this, null);
		}
	}


	public void removeOnChangeListener(OnContentChangeListener listener, String key)
	{
		Set<OnContentChangeListener> listenerSet = mOnChangeListeners.get(key);
		if (listenerSet != null)
		{
			listenerSet.remove(listener);
		}
	}


	private void notifyListeners(String key)
	{
		Set<OnContentChangeListener> listenerSet = mOnChangeListeners.get(key);
		if (listenerSet != null)
		{
			for (OnContentChangeListener listener : listenerSet)
			{
				listener.onContentChanged(this, key);
			}
		}
	}


	@Override
	public int describeContents()
	{
		return 0;
	}


	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeParcelable(mUri, flags);
		dest.writeParcelable(mBeforeContentValues, flags);
		dest.writeParcelable(mAfterContentValues, flags);
	}


	public void readFromParcel(Parcel source)
	{
		ClassLoader loader = getClass().getClassLoader();
		mUri = source.readParcelable(loader);
		mBeforeContentValues = source.readParcelable(loader);
		mAfterContentValues = source.readParcelable(loader);
	}

	public static final Parcelable.Creator<ContentSet> CREATOR = new Parcelable.Creator<ContentSet>()
	{
		public ContentSet createFromParcel(Parcel in)
		{
			final ContentSet state = new ContentSet();
			state.readFromParcel(in);
			return state;
		}


		public ContentSet[] newArray(int size)
		{
			return new ContentSet[size];
		}
	};
}
