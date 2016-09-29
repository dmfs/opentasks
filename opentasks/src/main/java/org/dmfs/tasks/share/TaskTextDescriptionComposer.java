/*
 * Copyright 2016 Marten Gajda <marten@dmfs.org>
 *
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

package org.dmfs.tasks.share;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;


/**
 * Creates a text description for a task.
 *
 * @author Gabor Keszthelyi
 */
public interface TaskTextDescriptionComposer
{
    /**
     * Creates a title for the task text description.
     */
    String title(ContentSet contentSet, Model model);

    /**
     * Creates the body of the task text description.
     */
    String body(ContentSet contentSet, Model model);

}
