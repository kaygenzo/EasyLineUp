/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.system

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.provider.DocumentsContract
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForText
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exporting goes through the system's folder picker, replaced here by espresso-intents: these
 * check the app asks for a folder and handles the answer. Writing the file into a real picked
 * folder is [ExportToRealFolderTest].
 */
@RunWith(AndroidJUnit4::class)
class ExportPickerTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val intentsRule = IntentsRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun openExportConfirmation() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText(string(R.string.title_settings).uppercase()).performClick()
        composeTestRule
            .onNodeWithText(string(R.string.setting_export_data_description))
            .performScrollTo()
            .performClick()
        composeTestRule.waitForText(string(R.string.export_confirm_title))
    }

    private fun chooseFolder(result: ActivityResult) {
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE)).respondWith(result)
        composeTestRule
            .onNodeWithText(string(R.string.export_confirm_action))
            .performScrollTo()
            .performClick()
    }

    @Test
    fun cancellingThePickerKeepsTheConfirmationOpenWithoutAnError() {
        chooseFolder(ActivityResult(Activity.RESULT_CANCELED, null))

        intended(hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(string(R.string.export_confirm_title)).assertExists()
        composeTestRule.onNodeWithText(string(R.string.settings_export_error_title))
            .assertDoesNotExist()
    }

    @Test
    fun aFolderTheAppCannotWriteToShowsTheExportError() {
        val notGranted = DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents",
            "primary:Documents"
        )

        chooseFolder(ActivityResult(Activity.RESULT_OK, Intent().setData(notGranted)))

        composeTestRule.waitForText(string(R.string.settings_export_error_title))
    }
}
