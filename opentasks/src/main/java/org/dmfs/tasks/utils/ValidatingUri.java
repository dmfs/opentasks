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

import android.net.Uri;
import androidx.annotation.Nullable;

import org.dmfs.jems.fragile.Fragile;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * A {@link Fragile} to create a valid {@link Uri} from a {@link Nullable} {@link CharSequence}.
 * <p>
 * Throws {@link URISyntaxException} for invalid or <code>null</code> or empty input.
 *
 * @author Gabor Keszthelyi
 */
public final class ValidatingUri implements Fragile<Uri, URISyntaxException>
{
    private final CharSequence mUriCandidate;


    public ValidatingUri(@Nullable CharSequence uriCandidate)
    {
        mUriCandidate = uriCandidate;
    }


    @Override
    public Uri value() throws URISyntaxException
    {
        if (mUriCandidate == null)
        {
            throw new URISyntaxException("null", "Uri input cannot be null");
        }
        if (mUriCandidate.length() == 0)
        {
            throw new URISyntaxException("", "Uri input cannot be empty");
        }
        return Uri.parse(new URI(mUriCandidate.toString()).toString());
    }
}
