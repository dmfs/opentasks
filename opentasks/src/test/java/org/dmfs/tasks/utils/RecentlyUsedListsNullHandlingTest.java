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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * This test is for checking that the bug <a href="https://github.com/dmfs/opentasks/issues/562">562</a>, which was caused by storing a "null" string in the
 * recently used list prefs string, like "null,3,2", is fixed correctly as far as {@link RecentlyUsedLists} is concerned. Calling {@link
 * RecentlyUsedLists#use(Context, long)} with <code>null</code> list id will still cause a NPE crash, but this fix will help to track down the original problem
 * if it happens again. It also checks that {@link RecentlyUsedLists} removes the "null" so we can possibly remove the code handling that later.
 *
 * @author Gabor Keszthelyi
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RecentlyUsedListsNullHandlingTest
{
    private static final String PREFERENCE_KEY = "RecentlyUsedLists";


    /**
     * That that the {@link RecentlyUsedLists#use(Context, long)} method cannot be called with <code>null</code> value.
     */
    @Test(expected = NullPointerException.class)
    public void test_use_cannot_be_called_with_null_listId()
    {
        Long listId = null;
        RecentlyUsedLists.use(RuntimeEnvironment.application, listId);
    }


    /**
     * Test that even if "null" string had been stored incorrectly previously, it doesn't affect the usage.
     */
    @Test
    public void test_getRecentFromList_that_the_stored_null_string_is_ignored()
    {
        Application context = RuntimeEnvironment.application;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        prefs.edit().putString(PREFERENCE_KEY, "null,1,2").apply();
        List<Long> allowedList = new ArrayList<>();
        allowedList.add(2L);
        assertThat(RecentlyUsedLists.getRecentFromList(context, allowedList), is(2L));

        prefs.edit().putString(PREFERENCE_KEY, "1,null,2").apply();
        allowedList = new ArrayList<>();
        allowedList.add(5L);
        assertThat(RecentlyUsedLists.getRecentFromList(context, allowedList), is(5L));

        prefs.edit().putString(PREFERENCE_KEY, "1,2,null").apply();
        allowedList = new ArrayList<>();
        allowedList.add(2L);
        assertThat(RecentlyUsedLists.getRecentFromList(context, allowedList), is(2L));
    }


    /**
     * Test that if "null" string had been stored incorrectly previously, it is cleared on the first time usage of {@link
     * RecentlyUsedLists#getRecentFromList(Context, List)}.
     * <p>
     * This is to ensure that we can remove the "null" handling fix later.
     */
    @Test
    public void test_getRecentFromList_that_the_stored_null_string_is_removed()
    {
        Application context = RuntimeEnvironment.application;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        List<Long> allowedList = new ArrayList<>();
        allowedList.add(2L);

        prefs.edit().putString(PREFERENCE_KEY, "null,1,2").apply();
        RecentlyUsedLists.getRecentFromList(context, allowedList);
        assertThat(prefs.getString(PREFERENCE_KEY, "na"), is("1,2"));

        prefs.edit().putString(PREFERENCE_KEY, "1,null,2").apply();
        RecentlyUsedLists.getRecentFromList(context, allowedList);
        assertThat(prefs.getString(PREFERENCE_KEY, "na"), is("1,2"));

        prefs.edit().putString(PREFERENCE_KEY, "null").apply();
        RecentlyUsedLists.getRecentFromList(context, allowedList);
        assertThat(prefs.getString(PREFERENCE_KEY, "na"), is(""));
    }


    /**
     * Test that if "null" string had been stored incorrectly previously, it is cleared on the first time usage of {@link
     * RecentlyUsedLists#use(Context, long)}.
     * <p>
     * This is to ensure that we can remove the "null" handling fix later.
     */
    @Test
    public void test_use_that_the_stored_null_string_is_removed()
    {
        Application context = RuntimeEnvironment.application;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        prefs.edit().putString(PREFERENCE_KEY, "null,1,2").apply();
        RecentlyUsedLists.use(context, 3L);
        assertThat(prefs.getString(PREFERENCE_KEY, "na"), is("3,1,2"));

        prefs.edit().putString(PREFERENCE_KEY, "1,null,2").apply();
        RecentlyUsedLists.use(context, 3L);
        assertThat(prefs.getString(PREFERENCE_KEY, "na"), is("3,1,2"));

        prefs.edit().putString(PREFERENCE_KEY, "null").apply();
        RecentlyUsedLists.use(context, 3L);
        assertThat(prefs.getString(PREFERENCE_KEY, "na"), is("3"));
    }


    /**
     * Test that <code>null</code> is not allowed in the <code>allowedList</code> {@link List} either.
     */
    @Test(expected = IllegalArgumentException.class)
    public void test_getRecentFromList_null_is_not_allowed_in_allowed_strings()
    {
        List<Long> allowedList = new ArrayList<>();
        allowedList.add(null);
        allowedList.add(1L);
        RecentlyUsedLists.getRecentFromList(RuntimeEnvironment.application, allowedList);
    }


    /**
     * Test that no value / empty value / null value stored in prefs is still handled correctly after the changes.
     */
    @Test
    public void test_getRecentFromList_empty_or_null_prefs_value_still_works()
    {
        Application context = RuntimeEnvironment.application;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        List<Long> allowedList = new ArrayList<>();
        allowedList.add(5L);

        assertThat(RecentlyUsedLists.getRecentFromList(context, allowedList), is(5L));

        prefs.edit().putString(PREFERENCE_KEY, null).apply();
        assertThat(RecentlyUsedLists.getRecentFromList(context, allowedList), is(5L));

        prefs.edit().putString(PREFERENCE_KEY, "").apply();
        assertThat(RecentlyUsedLists.getRecentFromList(context, allowedList), is(5L));
    }

}