/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.system

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.ExportData
import com.telen.easylineup.export.json.exportGson
import com.telen.easylineup.export.json.toJson
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.testing.ActivityTestUtils
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.inject
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForText
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Both "import" buttons open the system's file picker, replaced by espresso-intents answering
 * with a real exported file: from the Welcome screen of a fresh install, and from Settings.
 */
@RunWith(AndroidJUnit4::class)
class ImportPickerTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val intentsRule = IntentsRule()

    @get:Rule(order = 2)
    val composeTestRule = createEmptyComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val exportData: ExportData by inject()
    private val deleteAllData: DeleteAllData by inject()
    private val players: PlayerRepository by inject()

    @After
    fun closeScreens() {
        ActivityTestUtils.finishAll()
    }

    private fun exportedFile(): Uri {
        val json = runBlocking { exportGson.toJson(exportData().getOrThrow().toJson()) }
        val file = File(context.cacheDir, "picked.elu").apply { writeText(json) }
        return Uri.fromFile(file)
    }

    private fun pickFile(result: ActivityResult) {
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(result)
    }

    private fun confirmImport() {
        composeTestRule.waitForText(string(R.string.import_confirm_title))
        composeTestRule.onNodeWithText(string(R.string.import_confirm_action)).performClick()
        composeTestRule.waitForText(string(R.string.import_success_title))
        composeTestRule.onNodeWithText(string(R.string.generic_done)).performClick()
    }

    @Test
    fun importingFromTheWelcomeScreenRestoresTheTeamAndOpensHome() {
        pickFile(ActivityResult(Activity.RESULT_OK, Intent().setData(exportedFile())))
        runBlocking { deleteAllData().getOrThrow() }
        ActivityScenario.launch(LoginActivity::class.java)

        composeTestRule.onNodeWithText(string(R.string.login_import)).performScrollTo()
            .performClick()
        confirmImport()

        ActivityTestUtils.waitForResumed(HomeActivity::class.java)
        assertEquals(12, runBlocking { players.getPlayers() }.size)
    }

    @Test
    fun cancellingThePickerLeavesTheWelcomeScreen() {
        pickFile(ActivityResult(Activity.RESULT_CANCELED, null))
        runBlocking { deleteAllData().getOrThrow() }
        ActivityScenario.launch(LoginActivity::class.java)

        composeTestRule.onNodeWithText(string(R.string.login_import)).performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        ActivityTestUtils.waitForResumed(LoginActivity::class.java)
        composeTestRule.onNodeWithText(string(R.string.login_create)).assertExists()
    }

    @Test
    fun importingFromSettingsShowsTheResult() {
        pickFile(ActivityResult(Activity.RESULT_OK, Intent().setData(exportedFile())))
        ActivityScenario.launch(HomeActivity::class.java)
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText(string(R.string.title_settings).uppercase()).performClick()

        composeTestRule
            .onNodeWithText(string(R.string.setting_import_data_description))
            .performScrollTo()
            .performClick()

        confirmImport()
        composeTestRule.onNodeWithText(string(R.string.title_settings)).assertExists()
    }
}
