/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.system

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForText
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

/**
 * Exports into a real folder picked through the real system picker, driven with UiAutomator:
 * the only way to give [com.telen.easylineup.utils.ExportFileWriterImpl] a folder it is
 * actually allowed to write to. The picker's labels are matched in English, like the rest of
 * the instrumented tests' device setup.
 */
@RunWith(AndroidJUnit4::class)
class ExportToRealFolderTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val folder = "EasyLineUpExportTest"

    private val path = "/sdcard/Documents/$folder"

    @Before
    fun createTheFolderToPick() {
        device.executeShellCommand("rm -rf $path")
        device.executeShellCommand("mkdir -p $path")
    }

    @After
    fun removeTheFolder() {
        device.executeShellCommand("rm -rf $path")
    }

    /** Name and size only: neither the shell nor the app can read a file the picker created. */
    private fun writtenFiles(): List<Pair<String, Long>> =
        device.executeShellCommand("ls -l $path").lines()
            .map { it.trim().split(Regex("\\s+")) }
            .filter { it.size >= 8 && it.last().endsWith(".elu") }
            .map { it.last() to it[4].toLong() }

    private fun clickInPicker(label: String) {
        val pattern = Pattern.compile(label, Pattern.CASE_INSENSITIVE)
        val node = device.wait(Until.findObject(By.text(pattern)), PICKER_TIMEOUT_MILLIS)
        assertNotNull("\"$label\" not found in the system picker", node)
        node.click()
    }

    @Test
    fun exportingWritesAnEluFileIntoThePickedFolder() {
        OrientationTestUtils.rotateToPortrait()
        composeTestRule.waitForText(TestFixtures.TEAM_NAME)
        composeTestRule.onNodeWithText(string(R.string.title_settings).uppercase()).performClick()
        composeTestRule
            .onNodeWithText(string(R.string.setting_export_data_description))
            .performScrollTo()
            .performClick()
        composeTestRule.waitForText(string(R.string.export_confirm_title))
        composeTestRule
            .onNodeWithText(string(R.string.export_confirm_action))
            .performScrollTo()
            .performClick()

        clickInPicker("Documents")
        clickInPicker(folder)
        clickInPicker("Use this folder")
        clickInPicker("Allow")

        // The file shows up as soon as it's created, before its content is written. What that
        // content holds is covered by ExportImportRoundTripTest.
        composeTestRule.waitUntil(EXPORT_TIMEOUT_MILLIS) {
            writtenFiles().singleOrNull()?.second?.let { it > 0 } == true
        }
    }

    private companion object {
        const val PICKER_TIMEOUT_MILLIS = 10_000L
        const val EXPORT_TIMEOUT_MILLIS = 10_000L
    }
}
