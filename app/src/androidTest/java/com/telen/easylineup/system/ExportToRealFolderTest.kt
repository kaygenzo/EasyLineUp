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
 * actually allowed to write to. Nothing is matched on the picker's own labels, which follow
 * the device language: folders by their name, its buttons by their view id.
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

    private fun openFolder(name: String) {
        val node = device.wait(Until.findObject(By.text(name)), PICKER_TIMEOUT_MILLIS)
        assertNotNull("Folder \"$name\" not found in the system picker", node)
        node.click()
    }

    /** "Use this folder", the picker's own positive button. */
    private fun useThisFolder() {
        val node = device.wait(Until.findObject(By.res(POSITIVE_BUTTON)), PICKER_TIMEOUT_MILLIS)
        assertNotNull("The picker's \"Use this folder\" button was not found", node)
        node.click()
    }

    /**
     * "Allow" in the access confirmation dialog. Its button has the same id as "Use this
     * folder", still on screen behind it, so it is looked up inside the dialog only.
     */
    private fun allowAccess() {
        val title = device.wait(Until.findObject(By.res(DIALOG_TITLE)), PICKER_TIMEOUT_MILLIS)
        assertNotNull("The picker's access confirmation was not shown", title)
        var dialog = title
        while (dialog.parent != null) {
            dialog = dialog.parent
        }
        val allow = dialog.findObject(By.res(POSITIVE_BUTTON))
        assertNotNull("The access confirmation has no \"Allow\" button", allow)
        allow.click()
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

        openFolder("Documents")
        openFolder(folder)
        useThisFolder()
        allowAccess()

        // The file shows up as soon as it's created, before its content is written. What that
        // content holds is covered by ExportImportRoundTripTest.
        composeTestRule.waitUntil(EXPORT_TIMEOUT_MILLIS) {
            writtenFiles().singleOrNull()?.second?.let { it > 0 } == true
        }
    }

    private companion object {
        const val PICKER_TIMEOUT_MILLIS = 10_000L
        const val EXPORT_TIMEOUT_MILLIS = 10_000L
        const val POSITIVE_BUTTON = "android:id/button1"

        // The picker app is com.google.android.documentsui on Google images and
        // com.android.documentsui on AOSP ones.
        val DIALOG_TITLE: Pattern = Pattern.compile(".*documentsui:id/alertTitle")
    }
}
