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

/**
 * Convenience methods for manipulating bit flags.
 * <p>
 * Code from <a href="https://medium.com/@JakobUlbrich/flag-attributes-in-android-how-to-use-them-ac4ec8aee7d1">Flag Attributes in Android — How to Use Them</a>
 *
 * @author Gabor Keszthelyi
 */
public final class BitFlagUtils
{

    public static boolean containsFlag(int flagSet, int flag)
    {
        return (flagSet | flag) == flagSet;
    }


    public static int addFlag(int flagSet, int flag)
    {
        return flagSet | flag;
    }


    public static int toggleFlag(int flagSet, int flag)
    {
        return flagSet ^ flag;
    }


    public static int removeFlag(int flagSet, int flag)
    {
        return flagSet & (~flag);
    }


    private BitFlagUtils()
    {
    }
}
