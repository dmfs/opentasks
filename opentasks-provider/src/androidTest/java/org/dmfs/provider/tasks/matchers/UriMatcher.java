/*
 * Copyright 2019 dmfs GmbH
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

package org.dmfs.provider.tasks.matchers;

import android.net.Uri;

import org.hamcrest.Matcher;

import static org.dmfs.jems.hamcrest.matchers.LambdaMatcher.having;
import static org.hamcrest.Matchers.is;


/**
 * @author Marten Gajda
 */
public final class UriMatcher
{
    public static Matcher<Uri> scheme(String scheme)
    {
        return having(Uri::getScheme, is(scheme));
    }


    public static Matcher<Uri> authority(String authority)
    {
        return having(Uri::getEncodedAuthority, is(authority));
    }


    public static Matcher<Uri> path(Matcher<String> patchMatcher)
    {
        return having(Uri::getEncodedPath, patchMatcher);
    }

}
