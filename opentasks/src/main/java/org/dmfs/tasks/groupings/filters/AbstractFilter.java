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

package org.dmfs.tasks.groupings.filters;

import java.util.List;


/**
 * An abstract filter for child cursors in a grouped list.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface AbstractFilter
{

    /**
     * Append the selection part of this filter to a {@link StringBuilder}. This is much more efficiently when you're using a StringBuilder anyway.
     *
     * @param stringBuilder
     *         The {@link StringBuilder} where to append the selection string.
     */
    void getSelection(StringBuilder stringBuilder);

    /**
     * Append the selection arguments of this filter to a {@link List} of {@link String}s.
     *
     * @param selectionArgs
     *         The List where to append the arguments.
     */
    void getSelectionArgs(List<String> selectionArgs);
}
