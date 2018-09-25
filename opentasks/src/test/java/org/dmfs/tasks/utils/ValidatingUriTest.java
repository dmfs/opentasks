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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.URISyntaxException;

import static org.dmfs.tasks.utils.FragileMatcher.failsWith;
import static org.dmfs.tasks.utils.FragileMatcher.hasSuccessValue;
import static org.junit.Assert.assertThat;


/**
 * Unit test for {@link ValidatingUri}.
 *
 * @author Gabor Keszthelyi
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class ValidatingUriTest
{

    @Test
    public void testValidVariations()
    {
        assertThat(new ValidatingUri("http://abc.com"), hasSuccessValue(Uri.parse("http://abc.com")));
        assertThat(new ValidatingUri("https://abc.com/path"), hasSuccessValue(Uri.parse("https://abc.com/path")));
        assertThat(new ValidatingUri("tel:1234"), hasSuccessValue(Uri.parse("tel:1234")));
        assertThat(new ValidatingUri("mailto:example@abc.com"), hasSuccessValue(Uri.parse("mailto:example@abc.com")));
    }


    @Test
    public void testInValidVariations()
    {
        assertThat(new ValidatingUri(null), failsWith(URISyntaxException.class));
        assertThat(new ValidatingUri(""), failsWith(URISyntaxException.class));
        assertThat(new ValidatingUri("h h"), failsWith(URISyntaxException.class));
    }

}