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

import org.dmfs.jems.single.Single;

import java.util.Iterator;


/**
 * A Fragile is similar to a {@link Single} but it may throw an {@link Exception} during retrieval of the value.
 * <p>
 * It's primary use case is to allow deferring checked Exceptions if they can't be thrown right away. A common example is Iterating elements, {@link
 * Iterator#next()} doesn't allow checked Exceptions to be thrown. In this case a {@link Fragile} can be returned to defer any exception to evaluation time.
 *
 * @author Marten Gajda
 * @deprecated use it from jems when available
 */
@Deprecated
public interface Fragile<T, E extends Throwable>
{
    T value() throws E;
}
