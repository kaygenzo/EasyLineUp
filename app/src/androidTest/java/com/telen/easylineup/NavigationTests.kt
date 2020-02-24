package com.telen.easylineup

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab


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

//    @Test
//    fun navigateInAllScreensInPortrait() {
//
//        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
//
//        SystemClock.sleep(4000)
//
//        takeScreenshot("portrait_1_dashboard")
//        SystemClock.sleep(1000)
//
//        onView(withId(R.id.tileRecyclerView))
//                .perform(
//                        RecyclerViewActions.actionOnItemAtPosition<DashboardTileAdapter.TileViewHolder>(0, click())
//                )
//
//        SystemClock.sleep(1000)
//
//        takeScreenshot("portrait_2_players")
//        SystemClock.sleep(1000)
//
//        onView(withId(R.id.teamPlayersRecyclerView))
//                .perform(
//                        RecyclerViewActions.actionOnItemAtPosition<TeamAdapter.PlayerViewHolder>(0, click())
//                )
//
//        SystemClock.sleep(1000)
//
//        takeScreenshot("portrait_3_player_details")
//        SystemClock.sleep(1000)
//
//        onView(withId(R.id.action_edit))
//                .perform(click())
//
//        SystemClock.sleep(1000)
//
//        takeScreenshot("portrait_4_player_edit")
//        SystemClock.sleep(1000)
//
//        openDrawer()
//
//        SystemClock.sleep(1000)
//
//        onView(withId(R.id.nav_view))
//                .perform(
//                        NavigationViewActions.navigateTo(R.id.navigation_lineups)
//                )
//
//        SystemClock.sleep(1000)
//
//        takeScreenshot("portrait_5_list_lineups")
//        SystemClock.sleep(1000)
//
//        BaristaListInteractions.clickListItem(R.id.recyclerView, 6)
//        SystemClock.sleep(2000)
//
//        takeScreenshot("portrait_6_lineup_defense")
//        SystemClock.sleep(1000)
//
//        onView(allOf(withText("ATTACK"), isDescendantOfA(withId(R.id.lineupTabLayout))))
//                .perform(click())
//                .check(matches(isDisplayed()))
//
//        takeScreenshot("portrait_7_lineup_attack")
//        SystemClock.sleep(1000)
//
//        onView(withId(R.id.action_edit))
//                .perform(click())
//
//        takeScreenshot("portrait_8_lineup_edit_defense")
//        SystemClock.sleep(1000)
//
//        onView(allOf(withText("ATTACK"), isDescendantOfA(withId(R.id.lineupTabLayout))))
//                .perform(click())
//                .check(matches(isDisplayed()))
//
//        takeScreenshot("portrait_9_lineup_edit_attack")
//        SystemClock.sleep(1000)
//
//        openDrawer()
//
//        SystemClock.sleep(1000)
//
//        onView(allOf(withId(R.id.drawerImage), withParent(withId(R.id.navDrawerRootContainer))))
//                .perform(click())
//
//        SystemClock.sleep(1000)
//
//        takeScreenshot("portrait_10_edit_team")
//
//        SystemClock.sleep(1000)
//
////        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
////        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
//    }

    private fun takeScreenshot(name: String) {
        Screengrab.screenshot(name)
    }
}
