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
import android.content.Intent;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;


/**
 * Factory interface for creating an {@link Intent} for a Task.
 * Task is 'represented' by a {@link ContentSet} and a {@link Model} together.
 *
 * @author Gabor Keszthelyi
 */
public interface TaskIntentFactory
{
    /**
     * Creates an {@link Intent} for the given task ('represented' by <code>contentSet</code> and <code>model</code>)
     */
    Intent create(ContentSet contentSet, Model model, Context context);
}
