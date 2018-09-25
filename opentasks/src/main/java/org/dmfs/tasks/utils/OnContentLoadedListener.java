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

import android.content.ContentValues;


/**
 * A listener that is notified when an {@link AsyncContentLoader} has finished loading a set of {@link ContentValues}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface OnContentLoadedListener
{
    /**
     * Called when the {@link AsyncContentLoader} has loaded new {@link ContentValues}.
     *
     * @param values
     *         The loaded {@link ContentValues}.
     */
    void onContentLoaded(ContentValues values);
}
