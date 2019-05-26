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

package org.dmfs.tasks.groupings.cursorloaders;

import android.content.Context;
import android.database.Cursor;
import androidx.loader.content.Loader;


/**
 * An abstract factory that can create Loader<Cursor> instances.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class AbstractCursorLoaderFactory
{
    /**
     * Get a new {@link Loader} instance. Override this method to return a custom Loader for Cursors.
     *
     * @param context
     *         A {@link Context}.
     *
     * @return A brand new {@link Loader} for {@link Cursor}s.
     */
    public abstract Loader<Cursor> getLoader(Context context);
}
