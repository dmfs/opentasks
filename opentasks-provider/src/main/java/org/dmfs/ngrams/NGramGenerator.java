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

package org.dmfs.ngrams;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Generator for N-grams from a given String.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class NGramGenerator
{
    /**
     * A {@link Pattern} that matches anything that doesn't belong to a word or number.
     */
    private final static Pattern SEPARATOR_PATTERN = Pattern.compile("[^\\p{L}\\p{M}\\d]+");

    /**
     * A {@link Pattern} that matches anything that doesn't belong to a word.
     */
    private final static Pattern SEPARATOR_PATTERN_NO_NUMBERS = Pattern.compile("[^\\p{L}\\p{M}]+");

    private final int mN;
    private final int mMinWordLen;
    private boolean mAllLowercase = true;
    private boolean mReturnNumbers = true;
    private boolean mAddSpaceInFront = false;
    private Locale mLocale = Locale.getDefault();


    public NGramGenerator(int n)
    {
        this(n, 1);
    }


    public NGramGenerator(int n, int minWordLen)
    {
        mN = n;
        mMinWordLen = minWordLen;
    }


    /**
     * Set whether to convert all words to lower-case first.
     *
     * @param lowercase
     *         true to convert the test to lower case first.
     *
     * @return This instance.
     */
    public NGramGenerator setAllLowercase(boolean lowercase)
    {
        mAllLowercase = lowercase;
        return this;
    }


    /**
     * Set whether to index the beginning of a word with a space in front. This slightly raises the weight of word beginnings when searching.
     *
     * @param addSpace
     *         <code>true</code> to add a space in front of each word, <code>false</code> otherwise.
     *
     * @return This instance.
     */
    public NGramGenerator setAddSpaceInFront(boolean addSpace)
    {
        mAddSpaceInFront = addSpace;
        return this;
    }


    /**
     * Sets the {@link Locale} to use when converting the input string to lower case. This has no effect when {@link #setAllLowercase(boolean)} is called with
     * <code>false</code>.
     *
     * @param locale
     *         The {@link Locale} to user for the conversion to lower case.
     *
     * @return This instance.
     */
    public NGramGenerator setLocale(Locale locale)
    {
        mLocale = locale;
        return this;
    }


    /**
     * Get all N-grams contained in the given String.
     *
     * @param data
     *         The String to analyze.
     *
     * @return The {@link Set} containing the N-grams.
     */
    public Set<String> getNgrams(String data)
    {
        if (data == null)
        {
            return Collections.emptySet();
        }

        if (mAllLowercase)
        {
            data = data.toLowerCase(mLocale);
        }

        String[] words = mReturnNumbers ? SEPARATOR_PATTERN.split(data) : SEPARATOR_PATTERN_NO_NUMBERS.split(data);

        Set<String> set = new HashSet<String>(128);

        for (String word : words)
        {
            getNgrams(word, set);
        }

        return set;
    }


    private void getNgrams(String word, Set<String> ngrams)
    {
        final int len = word.length();

        if (len < mMinWordLen)
        {
            return;
        }

        final int n = mN;
        final int last = Math.max(1, len - n + 1);

        for (int i = 0; i < last; ++i)
        {
            ngrams.add(word.substring(i, Math.min(i + n, len)));
        }

        if (mAddSpaceInFront)
        {
            /*
             * Add another String with a space and the first n-1 characters of the word.
             */
            ngrams.add(" " + word.substring(0, Math.min(len, n - 1)));
        }
    }
}
