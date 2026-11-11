/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.gallery

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.testing.ActivityTestUtils
import com.telen.easylineup.testing.OrientationTestUtils
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.ThemeTestUtils
import com.telen.easylineup.testing.string
import com.telen.easylineup.testing.waitForAbsence
import com.telen.easylineup.testing.waitForText
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Walks through every screen once per display configuration and saves a real device screenshot
 * of each. They stay on the device until `./gradlew generateScreenGallery` (screen-gallery.gradle)
 * turns them into a single HTML page.
 *
 * Skipped unless the run asks for it, since it takes several minutes:
 * `./gradlew connectedDemoDebugAndroidTest -PscreenGallery`.
 *
 * It also proves every screen is reachable in every configuration: a control pushed off screen
 * by a small landscape layout fails the walk.
 */
@RunWith(Parameterized::class)
class ScreenGalleryTest(
    private val size: DisplaySize,
    private val orientation: Orientation,
    private val theme: Theme
) {
    /** Small and large match `@DugoutPreviews`; [PHONE] is the device's own display. */
    enum class DisplaySize(val pixels: String?, val density: Int?) {
        SMALL("720x1280", 320),
        PHONE(null, null),
        LARGE("1600x2560", 320)
    }

    enum class Orientation { PORTRAIT, LANDSCAPE }

    enum class Theme(val nightMode: Int) {
        DAY(AppCompatDelegate.MODE_NIGHT_NO),
        NIGHT(AppCompatDelegate.MODE_NIGHT_YES)
    }

    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private var screenIndex = 0

    @Before
    fun applyConfiguration() {
        assumeTrue(
            "Screen gallery not requested (-PscreenGallery)",
            InstrumentationRegistry.getArguments().getString(ARGUMENT) == "true"
        )
        device.executeShellCommand("mkdir -p $DEVICE_DIRECTORY")
        size.pixels?.let { device.executeShellCommand("wm size $it") }
        size.density?.let { device.executeShellCommand("wm density $it") }
        ThemeTestUtils.applyNightMode(theme.nightMode)
    }

    /**
     * Only once an Activity is on screen: on a phone-sized display, the home screen holds the
     * device in portrait, so a rotation requested from there never takes effect.
     */
    private fun rotate() {
        if (orientation == Orientation.LANDSCAPE) {
            OrientationTestUtils.rotateToLandscape()
        } else {
            OrientationTestUtils.rotateToPortrait()
        }
    }

    @After
    fun restoreDevice() {
        ActivityTestUtils.finishAll()
        ThemeTestUtils.resetNightMode()
        OrientationTestUtils.rotateToPortrait()
        device.executeShellCommand("wm density reset")
        device.executeShellCommand("wm size reset")
    }

    /** Runs as the shell user, so the files outlive the app's uninstall after the test run. */
    private fun capture(screen: String) {
        // Waits for the rotation itself rather than a fixed delay, then for Compose to settle.
        composeTestRule.waitUntil(timeoutMillis = ROTATION_TIMEOUT_MILLIS) {
            (device.displayWidth > device.displayHeight) ==
                (orientation == Orientation.LANDSCAPE)
        }
        composeTestRule.waitForIdle()
        val isLandscape = device.displayWidth > device.displayHeight
        check(isLandscape == (orientation == Orientation.LANDSCAPE)) {
            "$screen is not in $orientation: ${device.displayWidth}x${device.displayHeight}"
        }
        screenIndex++
        val index = screenIndex.toString().padStart(2, '0')
        val name = "${size}__${orientation}__${theme}__${index}_$screen".lowercase()
        device.executeShellCommand("screencap -p $DEVICE_DIRECTORY/$name.png")
    }

    private fun SemanticsNodeInteraction.scrollAndClick() {
        runCatching { performScrollTo() }
        performClick()
    }

    private fun text(text: String) = composeTestRule.onNodeWithText(text)

    private fun description(description: String) =
        composeTestRule.onNodeWithContentDescription(description)

    private fun <T : Activity> ActivityScenario<T>.captureAndClose(
        screen: String,
        waitFor: String
    ) {
        rotate()
        composeTestRule.waitForText(waitFor)
        capture(screen)
        close()
    }

    @Test
    fun captureEveryScreen() {
        ActivityScenario.launch(LoginActivity::class.java)
            .captureAndClose("welcome", string(R.string.login_create))
        ActivityScenario.launch(TeamCreationActivity::class.java)
            .captureAndClose("team_creation", string(R.string.team_form_title_create))

        ActivityScenario.launch(HomeActivity::class.java).use {
            rotate()
            composeTestRule.waitForText(TestFixtures.TEAM_NAME)
            capture("dashboard")
            captureRosterScreens()
            captureLineupScreens()
            captureSettingsScreens()
        }
    }

    private fun captureRosterScreens() {
        text(string(R.string.title_team).uppercase()).performClick()
        composeTestRule.waitForText("Starter 1")
        capture("roster")

        text("Starter 1").performClick()
        composeTestRule.waitForAbsence(string(R.string.roster_sort_by_name).uppercase())
        capture("player_profile")
        pressBack()
        composeTestRule.waitForText(string(R.string.roster_sort_by_name).uppercase())

        description(string(R.string.roster_add_player_content_description)).performClick()
        composeTestRule.waitForText(string(R.string.player_form_title_create))
        capture("player_creation")
        pressBack()
        composeTestRule.waitForText(string(R.string.generic_discard))
        text(string(R.string.generic_discard)).performClick()
        composeTestRule.waitForText(string(R.string.roster_sort_by_name).uppercase())
    }

    private fun captureLineupScreens() {
        text(string(R.string.title_lineups).uppercase()).performClick()
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)
        capture("tournaments")

        description(string(R.string.tournaments_list_stats_content_description)).performClick()
        composeTestRule.waitForText(string(R.string.title_tournament_statistics))
        capture("tournament_statistics")
        pressBack()
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)

        description(string(R.string.tournaments_list_create_lineup_content_description))
            .performClick()
        composeTestRule.waitForText(string(R.string.lineup_creation_header_title))
        capture("lineup_creation")
        pressBack()
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)

        text(TestFixtures.TOURNAMENT_NAME).performClick()
        composeTestRule.waitForText(TestFixtures.LINEUP_NAME)
        text(TestFixtures.LINEUP_NAME).scrollAndClick()
        composeTestRule.waitForText(string(R.string.new_lineup_tab_field_defense))
        capture("lineup_defense")

        text(string(R.string.new_lineup_tab_field_offense).uppercase()).performClick()
        composeTestRule.waitForText("Starter 1")
        capture("lineup_offense")

        text(string(R.string.action_edit)).performClick()
        composeTestRule.waitForText(string(R.string.action_save_lineup))
        capture("lineup_edition")
        pressBack()
        composeTestRule.waitForText(string(R.string.action_discard))
        text(string(R.string.action_discard)).performClick()
        composeTestRule.waitForAbsence(string(R.string.action_save_lineup))
        pressBack()
        composeTestRule.waitForText(TestFixtures.TOURNAMENT_NAME)
    }

    private fun captureSettingsScreens() {
        text(string(R.string.title_settings).uppercase()).performClick()
        composeTestRule.waitForText(string(R.string.title_settings))
        capture("settings")

        text(TestFixtures.TEAM_NAME).scrollAndClick()
        composeTestRule.waitForText(string(R.string.team_details_header_title))
        capture("team_details")
    }

    companion object {
        /** Set by app/build.gradle when Gradle runs with -PscreenGallery. */
        private const val ARGUMENT = "screenGallery"

        /** Also read by screen-gallery.gradle. */
        private const val DEVICE_DIRECTORY = "/data/local/tmp/easylineup_screens"

        /** Lets the last transition finish on the real display, which Compose idling ignores. */
        private const val ROTATION_TIMEOUT_MILLIS = 5_000L

        @JvmStatic
        @Parameterized.Parameters(name = "{0}_{1}_{2}")
        fun configurations(): List<Array<Any>> = DisplaySize.entries.flatMap { size ->
            Orientation.entries.flatMap { orientation ->
                Theme.entries.map { theme -> arrayOf<Any>(size, orientation, theme) }
            }
        }
    }
}
