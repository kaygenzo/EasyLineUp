/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Seeds (or clears) the real database before the test's Activity launches. Must be declared
 * with a lower `@Rule(order = ...)` than the `ComposeTestRule`/`ActivityScenarioRule` so it
 * runs first - otherwise the Activity's first DB read can race the seed:
 * ```
 * @get:Rule(order = 0) val seedRule = SeedDatabaseRule()
 * @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HomeActivity>()
 * ```
 */
class SeedDatabaseRule(
    private val mode: Mode = Mode.CORE_FIXTURE,
    private val teamType: TeamType = TeamType.BASEBALL,
    private val strategy: TeamStrategy = TeamStrategy.STANDARD
) : TestWatcher() {
    enum class Mode {
        CORE_FIXTURE,
        EMPTY,
        TEAM_WITHOUT_LINEUP,
        CORE_FIXTURE_WITH_SECOND_TEAM,
        LINEUP_VARIANT
    }

    lateinit var fixture: SeededFixture
        private set

    override fun starting(description: Description) {
        runBlocking {
            val seeder = DatabaseSeeder()
            when (mode) {
                Mode.CORE_FIXTURE -> fixture = seeder.seedCoreFixture()
                Mode.EMPTY -> seeder.clear()
                Mode.TEAM_WITHOUT_LINEUP -> fixture = seeder.seedTeamWithoutLineup()
                Mode.CORE_FIXTURE_WITH_SECOND_TEAM ->
                    fixture = seeder.seedCoreFixtureWithSecondTeam()
                Mode.LINEUP_VARIANT -> fixture = seeder.seedLineup(teamType, strategy)
            }
        }
    }
}
