/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.repository.RepositoryModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the migrations for which a pre-migration schema is available (the schemas directory
 * only started being committed at version 16 - see repository/.gitignore). Every migration added
 * from now on should get a matching test here.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate16To17_addsTournamentDateAndAddressColumnsWithDefaults() {
        helper.createDatabase(TEST_DB_NAME, 16).apply {
            execSQL(
                "INSERT INTO tournaments (id, name, createdAt, hash) " +
                    "VALUES (1, 'Tournament', 1000, 'hash-1')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            17,
            true,
            RepositoryModule.migration16To17()
        )

        db.query("SELECT startTime, endTime, address FROM tournaments WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0L, cursor.getLong(0))
            assertEquals(0L, cursor.getLong(1))
            assertTrue(cursor.isNull(2))
        }
    }

    companion object {
        private const val TEST_DB_NAME = "migration-test"
    }
}
