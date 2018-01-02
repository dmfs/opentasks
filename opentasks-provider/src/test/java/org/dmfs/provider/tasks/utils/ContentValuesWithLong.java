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

package org.dmfs.provider.tasks.utils;

import android.content.ContentValues;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.is;


/**
 * A {@link Matcher} to test if {@link ContentValues} contain a specific Long value.
 * <p>
 * TODO: can we convert that into a more generic {@link ContentValues} matcher? It might be useful in other places.
 * <p>
 * TODO: also consider moving this to "Test-Bolts"
 */
public final class ContentValuesWithLong extends FeatureMatcher<ContentValues, Long>
{
    private final String mKey;


    public ContentValuesWithLong(String valueKey, long value)
    {
        this(valueKey, is(value));
    }


    public ContentValuesWithLong(String valueKey, Matcher<Long> matcher)
    {
        super(matcher, "Long value " + valueKey, "Long value " + valueKey);
        mKey = valueKey;
    }


    @Override
    protected Long featureValueOf(ContentValues actual)
    {
        return actual.getAsLong(mKey);
    }
}
