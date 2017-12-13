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

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;


/**
 * {@link Matcher}s for {@link Fragile}.
 *
 * @author Gabor Keszthelyi
 */
// TODO Create proper versions in jems if Fragile is released
public final class FragileMatcher
{

    public static <T, E extends Throwable> Matcher<Fragile<T, E>> failsWith(Class<E> exceptionClass)
    {
        return new FragileExceptionMatcher<>(exceptionClass);
    }


    public static <T, E extends Throwable> Matcher<Fragile<T, E>> hasSuccessValue(T value)
    {
        return new FragileValueMatcher<>(CoreMatchers.equalTo(value));
    }


    private FragileMatcher()
    {

    }


    private static final class FragileValueMatcher<T, E extends Throwable> extends FeatureMatcher<Fragile<T, E>, T>
    {
        private FragileValueMatcher(Matcher<? super T> valueMatcher)
        {
            super(valueMatcher, "TODO", "TODO");
        }


        @Override
        protected T featureValueOf(Fragile<T, E> actual)
        {
            try
            {
                return actual.value();
            }
            catch (Throwable e)
            {
                throw new AssertionError("Fragile failed", e);
            }
        }

    }


    private static final class FragileExceptionMatcher<T, E extends Throwable> extends TypeSafeDiagnosingMatcher<Fragile<T, E>>
    {

        private final Class<E> mExceptionClass;


        private FragileExceptionMatcher(Class<E> exceptionClass)
        {
            mExceptionClass = exceptionClass;
        }


        @Override
        protected boolean matchesSafely(Fragile<T, E> fragile, Description mismatchDescription)
        {
            try
            {
                fragile.value();
            }
            catch (Throwable e)
            {
                if (mExceptionClass.isInstance(e))
                {
                    return true;
                }
            }
            return false;
        }


        @Override
        public void describeTo(Description description)
        {

        }

    }
}
