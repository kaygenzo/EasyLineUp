/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.splashscreen

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.ExportData
import com.telen.easylineup.export.json.exportGson
import com.telen.easylineup.export.json.toJson
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.testing.ActivityTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForText
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The import screens as a user meets them after opening an `.elu` file: preview, confirmation,
 * result, then wherever the app goes next. Starting from Welcome's "Import an existing team"
 * goes through the system's file picker first, which this doesn't cover.
 */
@RunWith(AndroidJUnit4::class)
class ImportFlowTest : KoinComponent {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val exportData: ExportData by inject()
    private val deleteAllData: DeleteAllData by inject()
    private val teams: TeamRepository by inject()
    private val players: PlayerRepository by inject()

    private val overwriteKey get() = context.getString(R.string.key_import_data_update_object)
    private val preferences get() = PreferenceManager.getDefaultSharedPreferences(context)

    @After
    fun cleanUp() {
        preferences.edit().remove(overwriteKey).commit()
        ActivityTestUtils.finishAll()
    }

    private fun exportToFile(): File {
        val json = runBlocking { exportGson.toJson(exportData().getOrThrow().toJson()) }
        return file(json)
    }

    private fun file(content: String) =
        File(context.cacheDir, "import_flow.elu").apply { writeText(content) }

    private fun open(file: File) {
        ActivityScenario.launch<ImportDataActivity>(
            Intent(context, ImportDataActivity::class.java).setData(Uri.fromFile(file))
        )
    }

    private fun confirmImport() {
        composeTestRule.waitForText(string(R.string.import_confirm_title))
        composeTestRule.onNodeWithText(string(R.string.import_confirm_action)).performClick()
        composeTestRule.waitForText(string(R.string.import_success_title))
        composeTestRule.onNodeWithText(string(R.string.generic_done)).performClick()
    }

    @Test
    fun confirmingRestoresTheFileAndOpensHome() {
        val file = exportToFile()
        runBlocking { deleteAllData().getOrThrow() }

        open(file)
        composeTestRule.waitForText(string(R.string.import_confirm_title))
        composeTestRule.onNodeWithText(file.name).assertExists()
        confirmImport()

        ActivityTestUtils.waitForResumed(HomeActivity::class.java)
        runBlocking {
            assertEquals(TestFixtures.TEAM_NAME, teams.getTeams().single().name)
            assertEquals(12, players.getPlayers().size)
        }
    }

    @Test
    fun cancellingImportsNothing() {
        val file = exportToFile()
        runBlocking { deleteAllData().getOrThrow() }

        open(file)
        composeTestRule.waitForText(string(R.string.import_confirm_title))
        composeTestRule.onNodeWithText(string(R.string.cancel_button)).performClick()
        composeTestRule.waitForIdle()

        assertTrue(runBlocking { teams.getTeams() }.isEmpty())
    }

    @Test
    fun overwriteToggleDecidesWhetherLocalChangesSurvive() {
        val file = exportToFile()
        val playerId = seedRule.fixture.playerIds.first()
        runBlocking {
            players.updatePlayer(players.getPlayerById(playerId).copy(name = "Renamed Locally"))
        }

        open(file)
        composeTestRule.waitForText(string(R.string.import_confirm_title))
        composeTestRule.onNode(isToggleable()).performClick()
        confirmImport()

        ActivityTestUtils.waitForResumed(HomeActivity::class.java)
        assertEquals("Starter 1", runBlocking { players.getPlayerById(playerId) }.name)
        assertTrue(preferences.getBoolean(overwriteKey, false))
    }

    @Test
    fun aFileWithoutAnyTeamLeadsToTeamCreation() {
        runBlocking { deleteAllData().getOrThrow() }

        open(file("""{"teams":[]}"""))
        confirmImport()

        ActivityTestUtils.waitForResumed(TeamCreationActivity::class.java)
    }

    @Test
    fun aCorruptFileShowsTheErrorDialogAndImportsNothing() {
        runBlocking { deleteAllData().getOrThrow() }

        open(file("not valid json at all"))

        onView(withText(string(R.string.settings_import_error_title)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        assertTrue(runBlocking { teams.getTeams() }.isEmpty())
    }
}
