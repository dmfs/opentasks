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

package org.dmfs.tasks.utils.factory;

/**
 * Represents a lazily created instance of type <code>T</code>.
 *
 * @author Gabor Keszthelyi
 */
// TODO Use it from dmfs java tools library when available
public interface Lazy<T>
{
    /**
     * Returns the instance which is created on first access.
     */
    T get();
}
