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

package org.dmfs.tasks.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.dmfs.tasks.utils.AsyncContentLoader;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnContentLoadedListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * A ContentSet takes care of loading and storing the values for a specific {@link Uri}.
 * <p>
 * This class is {@link Parcelable} to allow storing it in a Bundle.
 * </p>
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ContentSet implements OnContentLoadedListener, Parcelable
{
    private static final String TAG = "ContentSet";

    /**
     * The {@link ContentValues} that have been read from the database (or <code>null</code> for insert operations).
     */
    private ContentValues mBeforeContentValues;

    /**
     * The {@link ContentValues} that have been modified.
     */
    private ContentValues mAfterContentValues;

    /**
     * The {@link Uri} we operate on. For insert operations this is a directory URI, otherwise it has to be an item URI.
     */
    private Uri mUri;

    /**
     * A {@link Map} for the {@link OnContentChangeListener}s. A listener registers for a specific key in a content set or for <code>null</code> to e notified
     * of full reloads.
     */
    private final Map<String, Set<OnContentChangeListener>> mOnChangeListeners = new HashMap<String, Set<OnContentChangeListener>>();

    /**
     * A counter for the number of bulk updates currently running. It is incremented on {@link #startBulkUpdate()} and decremented on
     * {@link #finishBulkUpdate()}. If this values becomes <code>null</code> in {@link #finishBulkUpdate()} all listeners get notified.
     */
    private int mBulkUpdates = 0;

    /**
     * Holds all {@link OnContentChangeListener}s that need to be notified, because something has changed during a bulk update.
     */
    private final Set<OnContentChangeListener> mPendingNotifications = new HashSet<OnContentChangeListener>();

    /**
     * Indicates that loading is in process.
     */
    private boolean mLoading = false;


    /**
     * Private constructor that is used when creating a ContentSet form a parcel.
     */
    private ContentSet()
    {
    }


    /**
     * Create a new ContentSet for a specific {@link Uri}. <code>uri</code> is either a directory URI or an item URI. To load the content of an item URI call
     * {@link #update(Context, ContentValueMapper)}.
     *
     * @param uri
     *         A content URI, either a directory URI or an item URI.
     */
    public ContentSet(Uri uri)
    {
        if (uri == null)
        {
            throw new IllegalArgumentException("uri must not be null");
        }

        mUri = uri;
    }


    /**
     * Clone constructor.
     *
     * @param other
     *         The {@link ContentSet} to clone.
     */
    public ContentSet(ContentSet other)
    {
        if (other == null)
        {
            throw new IllegalArgumentException("other must not be null");
        }

        if (other.mBeforeContentValues != null)
        {
            mBeforeContentValues = new ContentValues(other.mBeforeContentValues);
        }

        if (other.mAfterContentValues != null)
        {
            mAfterContentValues = new ContentValues(other.mAfterContentValues);
        }

        mUri = other.mUri;
    }


    /**
     * Load the content. This method must not be called if the URI of this ContentSet is a directory URI and it has not been persited yet.
     *
     * @param context
     *         A context.
     * @param mapper
     *         The {@link ContentValueMapper} to use when loading the values.
     */
    public void update(Context context, ContentValueMapper mapper)
    {
        String itemType = context.getContentResolver().getType(mUri);
        if (itemType != null && !itemType.startsWith(ContentResolver.CURSOR_DIR_BASE_TYPE))
        {
            mLoading = true;
            new AsyncContentLoader(context, this, mapper).execute(mUri);
        }
        else
        {
            throw new UnsupportedOperationException("Can not load content from a directoy URI: " + mUri);
        }
    }


    @Override
    public void onContentLoaded(ContentValues values)
    {
        mBeforeContentValues = values;
        mLoading = false;
        notifyLoadedListeners();
    }


    /**
     * Returns whether this {@link ContentSet} is currently loading values.
     *
     * @return <code>true</code> is an asynchronous loading operation is in progress, <code>false</code> otherwise.
     */
    public boolean isLoading()
    {
        return mLoading;
    }


    /**
     * Delete this content. This ContentSet can no longer be used after this method has been called!
     *
     * @param context
     *         A context.
     */
    public void delete(Context context)
    {
        if (mUri != null)
        {
            String itemType = context.getContentResolver().getType(mUri);
            if (itemType != null && !itemType.startsWith(ContentResolver.CURSOR_DIR_BASE_TYPE))
            {
                context.getContentResolver().delete(mUri, null, null);
                mBeforeContentValues = null;
                mAfterContentValues = null;
                mUri = null;
            }
            else
            {
                throw new UnsupportedOperationException("Can not load delete a directoy URI: " + mUri);
            }
        }
        else
        {
            Log.w(TAG, "Trying to delete empty ContentSet");
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

        mAfterContentValues = null;

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
        Integer oldValue = getAsInteger(key);
        if (value != null && !value.equals(oldValue) || value == null && oldValue != null)
        {
            if (mBeforeContentValues != null && mBeforeContentValues.containsKey(key))
            {
                Integer beforeValue = mBeforeContentValues.getAsInteger(key);
                if (beforeValue != null && beforeValue.equals(value) || beforeValue == null && value == null)
                {
                    // value equals before value, so remove it from after values
                    mAfterContentValues.remove(key);
                    notifyUpdateListeners(key);
                    return;
                }
            }
            // value has changed, update
            ensureAfter().put(key, value);
            notifyUpdateListeners(key);
        }
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
        Long oldValue = getAsLong(key);
        if (value != null && !value.equals(oldValue) || value == null && oldValue != null)
        {
            if (mBeforeContentValues != null && mBeforeContentValues.containsKey(key))
            {
                Long beforeValue = mBeforeContentValues.getAsLong(key);
                if (beforeValue != null && beforeValue.equals(value) || beforeValue == null && value == null)
                {
                    // value equals before value, so remove it from after values
                    mAfterContentValues.remove(key);
                    notifyUpdateListeners(key);
                    return;
                }
            }
            ensureAfter().put(key, value);
            notifyUpdateListeners(key);
        }
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
        String oldValue = getAsString(key);
        if (value != null && !value.equals(oldValue) || value == null && oldValue != null)
        {
            if (mBeforeContentValues != null && mBeforeContentValues.containsKey(key))
            {
                String beforeValue = mBeforeContentValues.getAsString(key);
                if (beforeValue != null && beforeValue.equals(value) || beforeValue == null && value == null)
                {
                    // value equals before value, so remove it from after values
                    mAfterContentValues.remove(key);
                    notifyUpdateListeners(key);
                    return;
                }
            }
            ensureAfter().put(key, value);
            notifyUpdateListeners(key);
        }
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


    public void put(String key, Float value)
    {
        Float oldValue = getAsFloat(key);
        if (value != null && !value.equals(oldValue) || value == null && oldValue != null)
        {
            if (mBeforeContentValues != null && mBeforeContentValues.containsKey(key))
            {
                Float beforeValue = mBeforeContentValues.getAsFloat(key);
                if (beforeValue != null && beforeValue.equals(value) || beforeValue == null && value == null)
                {
                    // value equals before value, so remove it from after values
                    mAfterContentValues.remove(key);
                    notifyUpdateListeners(key);
                    return;
                }
            }
            ensureAfter().put(key, value);
            notifyUpdateListeners(key);
        }
    }


    public Float getAsFloat(String key)
    {
        final ContentValues after = mAfterContentValues;
        if (after != null && after.containsKey(key))
        {
            return mAfterContentValues.getAsFloat(key);
        }
        return mBeforeContentValues == null ? null : mBeforeContentValues.getAsFloat(key);
    }


    /**
     * Returns the Uri this {@link ContentSet} is read from (or has been written to). This may be a directory {@link Uri} if the ContentSet has not been stored
     * yet.
     *
     * @return The {@link Uri}.
     */
    public Uri getUri()
    {
        return mUri;
    }


    /**
     * Start a new bulk update. You should use this when you update multiple values at once and you don't want to send an update notification every time. When
     * you're done call {@link #finishBulkUpdate()} which sned the notifications (unless there is another bulk update in progress).
     */
    public void startBulkUpdate()
    {
        ++mBulkUpdates;
    }


    /**
     * Finish a bulk update and notify all listeners of values that have been changed (unless there is still another bilk update in progress).
     */
    public void finishBulkUpdate()
    {
        if (mBulkUpdates == 1)
        {
            Set<OnContentChangeListener> listeners = new HashSet<OnContentChangeListener>(mPendingNotifications);
            mPendingNotifications.clear();
            for (OnContentChangeListener listener : listeners)
            {
                listener.onContentChanged(this);
            }
        }
        --mBulkUpdates;
    }


    /**
     * Remove the value with the given key from the ContentSet. This is actually replacing the value by <code>null</code>.
     *
     * @param key
     *         The key of the value to remove.
     */
    public void remove(String key)
    {
        if (mAfterContentValues != null)
        {
            mAfterContentValues.putNull(key);
        }
        else if (mBeforeContentValues != null && mBeforeContentValues.get(key) != null)
        {
            ensureAfter().putNull(key);
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
            listener.onContentLoaded(this);
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


    private void notifyUpdateListeners(String key)
    {
        Set<OnContentChangeListener> listenerSet = mOnChangeListeners.get(key);
        if (listenerSet != null)
        {
            for (OnContentChangeListener listener : listenerSet)
            {
                if (mBulkUpdates > 0)
                {
                    mPendingNotifications.add(listener);
                }
                else
                {
                    listener.onContentChanged(this);
                }
            }
        }
    }


    private void notifyLoadedListeners()
    {
        Set<OnContentChangeListener> listenerSet = mOnChangeListeners.get(null);
        if (listenerSet != null)
        {
            for (OnContentChangeListener listener : listenerSet)
            {
                listener.onContentLoaded(this);
            }
        }
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
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
