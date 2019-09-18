package com.telen.easylineup

import android.content.pm.ActivityInfo
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule

import org.junit.runner.RunWith

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.contrib.RecyclerViewActions
import com.telen.easylineup.dashboard.DashboardTileAdapter
import com.telen.easylineup.dashboard.models.TeamSizeData
import org.hamcrest.Matchers.*

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.schibsted.spain.barista.interaction.BaristaClickInteractions
import com.schibsted.spain.barista.interaction.BaristaDrawerInteractions.openDrawer
import com.schibsted.spain.barista.interaction.BaristaListInteractions
import com.schibsted.spain.barista.interaction.BaristaMenuClickInteractions
import com.telen.easylineup.team.TeamAdapter
import kotlinx.android.synthetic.main.fragment_player_list.view.*
import org.junit.*
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy



/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationTests {

    @get:Rule
    var activityRule: ActivityTestRule<HomeActivity> = ActivityTestRule(HomeActivity::class.java)

    companion object {
        @Before fun init() {

        }
    }

    //@ClassRule var localeTestRule: LocaleTestRule = LocaleTestRule()

    @Test
    fun navigateInAllScreensInPortrait() {

        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

        SystemClock.sleep(4000)

        takeScreenshot("portrait_1_dashboard")
        SystemClock.sleep(1000)

        onView(withId(R.id.tileRecyclerView))
                .perform(
                        RecyclerViewActions.actionOnItemAtPosition<DashboardTileAdapter.TileViewHolder>(0, click())
                )

        SystemClock.sleep(1000)

        takeScreenshot("portrait_2_players")
        SystemClock.sleep(1000)

        onView(withId(R.id.teamPlayersRecyclerView))
                .perform(
                        RecyclerViewActions.actionOnItemAtPosition<TeamAdapter.PlayerViewHolder>(0, click())
                )

        SystemClock.sleep(1000)

        takeScreenshot("portrait_3_player_details")
        SystemClock.sleep(1000)

        onView(withId(R.id.action_edit))
                .perform(click())

        SystemClock.sleep(1000)

        takeScreenshot("portrait_4_player_edit")
        SystemClock.sleep(1000)

        openDrawer()

        SystemClock.sleep(1000)

        onView(withId(R.id.nav_view))
                .perform(
                        NavigationViewActions.navigateTo(R.id.navigation_lineups)
                )

        SystemClock.sleep(1000)

        takeScreenshot("portrait_5_list_lineups")
        SystemClock.sleep(1000)

        BaristaListInteractions.clickListItem(R.id.recyclerView, 6)
        SystemClock.sleep(2000)

        takeScreenshot("portrait_6_lineup_defense")
        SystemClock.sleep(1000)

        onView(allOf(withText("ATTACK"), isDescendantOfA(withId(R.id.lineupTabLayout))))
                .perform(click())
                .check(matches(isDisplayed()))

        takeScreenshot("portrait_7_lineup_attack")
        SystemClock.sleep(1000)

        onView(withId(R.id.action_edit))
                .perform(click())

        takeScreenshot("portrait_8_lineup_edit_defense")
        SystemClock.sleep(1000)

        onView(allOf(withText("ATTACK"), isDescendantOfA(withId(R.id.lineupTabLayout))))
                .perform(click())
                .check(matches(isDisplayed()))

        takeScreenshot("portrait_9_lineup_edit_attack")
        SystemClock.sleep(1000)

        openDrawer()

        SystemClock.sleep(1000)

        onView(allOf(withId(R.id.drawerImage), withParent(withId(R.id.navDrawerRootContainer))))
                .perform(click())

        SystemClock.sleep(1000)

        takeScreenshot("portrait_10_edit_team")

        SystemClock.sleep(1000)

//        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
    }

    private fun takeScreenshot(name: String) {
        Screengrab.screenshot(name)
    }
}
