/*
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

package org.dmfs.tasks.model.defaults;

import org.dmfs.tasks.model.ContentSet;


/**
 * Defines a default value of a specific type and a method that generates the respective value.
 *
 * @param <T>
 *         Type of the default value to be generated
 */
public interface Default<T>
{
    /**
     * Generates a default value of a specific type with respect to the current state of other values and a generic default.
     *
     * @param currentValues
     *         Other values in the {@link ContentSet} can be used in order to generate the default value.
     * @param genericDefault
     *         A generic default value which can be used as fall-back if the {@link ContentSet} gives no clue.
     *
     * @return Value of type <code>T</code>
     */
    T getCustomDefault(ContentSet currentValues, T genericDefault);
}
