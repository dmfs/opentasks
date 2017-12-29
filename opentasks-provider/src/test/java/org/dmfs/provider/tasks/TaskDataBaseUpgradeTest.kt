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

package org.dmfs.provider.tasks


import android.database.sqlite.SQLiteOpenHelper
import org.dmfs.provider.tasks.TaskDatabaseHelper.Tables.TASKS
import org.dmfs.tasks.contract.TaskContract.Tasks.*
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Test for checking the SQLite database upgrades.
 *
 * @author Gabor Keszthelyi
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TaskDataBaseUpgradeTest {


    /**
     * On upgrading to version 20 the timezones of tasks with [IS_ALLDAY] true have to be replaced with `null`.
     */
    @Test
    fun test_20_allDayTimeZonesToNull() {

        val dbHelper: SQLiteOpenHelper = TaskDatabaseHelper(RuntimeEnvironment.application, null)

        val expectedTimeZones: Map<Long, String?> = dbHelper.writableDatabase.use {

            val lId = LIST_ID to 42 // LIST_ID is a required column in the table

            mapOf(
                    it.insert(TASKS, IS_ALLDAY to "1", TZ to "UTC", lId) to null,

                    it.insert(TASKS, IS_ALLDAY to "1", TZ to "Europe/Berlin", lId) to null,

                    it.insert(TASKS, IS_ALLDAY to "1", TZ to null, lId) to null,

                    it.insert(TASKS, IS_ALLDAY to "0", TZ to "Europe/Paris", lId) to "Europe/Paris",

                    it.insert(TASKS, IS_ALLDAY to null, TZ to "America/Chicago", lId) to "America/Chicago",

                    it.insert(TASKS, IS_ALLDAY to null, TZ to null, lId) to null
            )
        }

        dbHelper.writableDatabase.use {
            dbHelper.onUpgrade(it, 19, 20)
        }

        dbHelper.readableDatabase.use {
            val size = it.select(TASKS)
                    .columns(_ID, TZ)
                    .parseList(object : MapRowParser<String> {

                        override fun parseRow(columns: Map<String, Any?>): String {

                            // Assert that the actual and expected time zones are the same:
                            assertThat(expectedTimeZones[columns[_ID]], equalTo(columns[TZ]))

                            return "ignored";
                        }
                    }).size

            assertThat(size, not(0)) // Just to make sure that there were rows actually
        }
    }
}