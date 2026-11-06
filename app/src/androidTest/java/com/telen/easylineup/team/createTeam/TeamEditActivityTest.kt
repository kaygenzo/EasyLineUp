/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.testing.string
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke coverage for `TeamCreationActivity`/[TeamFormScreen] in its edit-team mode (reached from
 * `TeamDetailsScreen`'s edit action, via the `Constants.EXTRA_TEAM` intent extra) - render and
 * pre-fill only, per this screen's Phase 3 depth policy. [TeamCreationActivityTest] already
 * covers the create-mode path (no extra) in depth; the ViewModel-side wiring (`SetupViewModel.
 * setTeam`) is already unit-tested, so the risk this closes is specifically the Compose
 * rendering/pre-fill path, never previously exercised in edit mode.
 *
 * Launched with a custom [Intent] rather than [androidx.compose.ui.test.junit4.
 * createAndroidComposeRule], which only supports a bare launch intent - no `SeedDatabaseRule`
 * is needed since this test never saves, only renders what the Activity does with the Team it
 * was handed.
 */
@RunWith(AndroidJUnit4::class)
class TeamEditActivityTest {
    private val existingTeam = Team(
        id = 1L,
        name = "Riverside Hawks",
        type = TeamType.BASEBALL.id,
        main = true
    )

    @get:Rule
    val composeTestRule = AndroidComposeTestRule(
        activityRule = ActivityScenarioRule<TeamCreationActivity>(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                TeamCreationActivity::class.java
            ).putExtra(Constants.EXTRA_TEAM, existingTeam)
        ),
        activityProvider = { rule -> rule.scenario.getActivitySync() }
    )

    @Test
    fun rendersEditTitleWithThePassedTeamPreFilled() {
        composeTestRule.onNodeWithText(string(R.string.team_form_title_edit)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Riverside Hawks").assertIsDisplayed()
    }
}

private fun <A : android.app.Activity> ActivityScenario<A>.getActivitySync(): A {
    var activity: A? = null
    onActivity { activity = it }
    return requireNotNull(activity)
}
