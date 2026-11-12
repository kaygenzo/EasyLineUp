/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository

import com.telen.easylineup.repository.dao.DATABASE_VERSION
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The chain the database builder registers: a migration written but not added to
 * [Migrations.all] would only fail on a user's device, upgrading from an older version.
 */
class MigrationsTest {
    @Test
    fun theChainGoesFromVersionOneToTheCurrentOneWithoutGaps() {
        val steps = Migrations.all.map { it.startVersion to it.endVersion }

        assertEquals((1 until DATABASE_VERSION).map { it to it + 1 }, steps)
    }
}
