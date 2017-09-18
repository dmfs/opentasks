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

package org.dmfs.opentaskspal.utils;

/**
 * Backport of Java 8 {@link java.util.Objects}.
 *
 * @author Gabor Keszthelyi
 */
public class Objects
{
    /**
     * @see java.util.Objects#equals(Object, Object)
     */
    public static boolean equals(Object o1, Object o2)
    {
        return o1 == o2 || o1 != null && o1.equals(o2);
    }
}
