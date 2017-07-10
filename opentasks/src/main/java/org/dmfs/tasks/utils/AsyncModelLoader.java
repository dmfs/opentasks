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

package org.dmfs.tasks.utils;

import android.content.Context;
import android.os.AsyncTask;

import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.Sources;

import java.lang.ref.WeakReference;


/**
 * An asynchronous model loader. Loads a specific model in the background and notifies a listener when the operation is finished.
 * <p>
 * If there is no model for a specific account type a default model is returned.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class AsyncModelLoader extends AsyncTask<String, Void, Model>
{
    /**
     * Stores the listener in a {@link WeakReference}. The loader may take longer to load than the lister lives. We don't want to prevent the listener from
     * being garbage collected.
     */
    private WeakReference<OnModelLoadedListener> mListener;

    /**
     * The {@link Context} we're running in, stored in a {@link WeakReference}.
     */
    private WeakReference<Context> mContext;


    /**
     * Create a new AsyncModelLoader.
     *
     * @param context
     *         The application context.
     * @param listener
     *         The {@link OnModelLoadedListener} that receives the model.
     */
    public AsyncModelLoader(Context context, OnModelLoadedListener listener)
    {
        mContext = new WeakReference<Context>(context);
        mListener = new WeakReference<OnModelLoadedListener>(listener);
    }


    @Override
    protected final Model doInBackground(String... accountTypes)
    {
        final OnModelLoadedListener target = mListener.get();
        final Context context = mContext.get();

        if (target != null)
        {
            Sources sources = Sources.getInstance(context);
            Model model = sources.getModel(accountTypes[0]);
            if (model == null)
            {
                model = sources.getDefaultModel();
            }
            return model;
        }
        else
        {
            return null;
        }
    }


    @Override
    protected final void onPostExecute(Model model)
    {
        final OnModelLoadedListener target = mListener.get();
        if (target != null)
        {
            target.onModelLoaded(model);
        }
    }
}
