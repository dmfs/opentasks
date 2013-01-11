/*
 * AsyncContentLoader.java
 *
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

package org.dmfs.tasks.utils;

import java.lang.ref.WeakReference;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

/**
 * An asynchronous content loader. Loads all values of the given {@link Uri}s asynchronously and notifies a listener
 * when the operation is finished.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class AsyncContentLoader extends AsyncTask<Uri, Void, ContentValues[]> {

    private WeakReference<OnContentLoadedListener> mListener;
    private ContentValueMapper mMapper;
    private Context mContext;

    public AsyncContentLoader(Context context, OnContentLoadedListener listener, ContentValueMapper mapper) {
	mContext = context;
	mListener = new WeakReference<OnContentLoadedListener>(listener);
	mMapper = mapper;
    }

    @Override
    protected final ContentValues[] doInBackground(Uri... params) {
	final OnContentLoadedListener target = mListener.get();

	if (target != null) {

	    ContentValues[] result = new ContentValues[params.length];

	    ContentResolver resolver = mContext.getContentResolver();

	    int len = params.length;
	    for (int i = 0; i < len; ++i) {
		Cursor c = resolver.query(params[i], mMapper.getColumns(), null, null, null);
		try {
		    if (c != null) {
			if (c.moveToNext()) {
			    result[i] = mMapper.map(c);
			}
		    }
		} finally {
		    c.close();
		}
	    }
	    return result;
	} else {
	    return null;
	}
    }

    @Override
    protected final void onPostExecute(ContentValues[] result) {
	final OnContentLoadedListener target = mListener.get();
	if (target != null) {
	    if (result != null) {
		for (ContentValues values : result) {
		    target.onContentLoaded(values);
		}
	    }
	}
    }
}
