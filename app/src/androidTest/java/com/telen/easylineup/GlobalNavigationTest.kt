package com.telen.easylineup


import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions
import com.schibsted.spain.barista.interaction.BaristaClickInteractions
import com.schibsted.spain.barista.interaction.BaristaDrawerInteractions.openDrawer
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions
import com.schibsted.spain.barista.interaction.BaristaViewPagerInteractions
import com.schibsted.spain.barista.rule.cleardata.ClearPreferencesRule
import com.telen.easylineup.dashboard.DashboardTileAdapter
import com.telen.easylineup.splashscreen.SplashScreenActivity
import com.telen.easylineup.team.TeamAdapter
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy


@LargeTest
@RunWith(AndroidJUnit4::class)
class GlobalNavigationTest {

    private val delay = 500L

    @Rule
    @JvmField
    var mSplashScreenTestRule = ActivityTestRule(SplashScreenActivity::class.java)

    @Rule
    @JvmField
    var mHomeTestRule = ActivityTestRule(HomeActivity::class.java)

    @Rule
    @JvmField
    var clearPreferencesRule = ClearPreferencesRule()

//    @Rule
//    @JvmField
//    var clearDatabaseRule = ClearDatabaseRule()

    @Before
    fun init() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
    }

    private fun takeScreenshot(name: String, activity: Activity) {
        ScreenShotter.takeScreenshot(name, activity)
        //Screengrab.screenshot(name)
    }

    private fun initialization() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        takeScreenshot("dashboard", mHomeTestRule.activity)

        openDrawer()

        Thread.sleep(delay)

        //click on tap tap view button
        val tapTargetView = onView(
                allOf(withClassName(`is`("com.getkeepsafe.taptargetview.TapTargetView")), isDisplayed()))
        tapTargetView.perform(click())

        Thread.sleep(delay)
    }

    @Test
    fun applyPlayersManagementTests() {

        initialization()

        //go to team players
        onView(withId(R.id.nav_view))
                .perform(
                        NavigationViewActions.navigateTo(R.id.navigation_team)
                )

        takeScreenshot("players", mHomeTestRule.activity)

        Thread.sleep(delay)

        applyRotation("players")

        Thread.sleep(delay)

        //click on first player Albert
        onView(withId(R.id.teamPlayersRecyclerView))
                .perform(
                        RecyclerViewActions.actionOnItemAtPosition<TeamAdapter.PlayerViewHolder>(0, click())
                )

        Thread.sleep(delay)

        takeScreenshot("albert_details", mHomeTestRule.activity)

        //check name is ALBERT
        BaristaVisibilityAssertions.assertDisplayed(R.id.playerName, "ALBERT")

        Thread.sleep(delay)

        applyRotation("albert_details")

        Thread.sleep(delay)

        //click on edit action button
        onView(withId(R.id.action_edit))
                .perform(click())

        Thread.sleep(delay)

        takeScreenshot("albert_edit", mHomeTestRule.activity)

        applyRotation("albert_edit")

        Thread.sleep(delay)

        // 1. Check save button come back on details
        BaristaClickInteractions.clickOn("Save")

        Thread.sleep(delay)

        //check name is albert
        BaristaVisibilityAssertions.assertDisplayed(R.id.playerName, "ALBERT")

        Thread.sleep(delay)

        onView(withId(R.id.action_edit))
                .perform(click())

        Thread.sleep(delay)

        // 2. Check cancel button come back on details
        BaristaClickInteractions.clickOn("Cancel")

        Thread.sleep(delay)

        //check name is albert
        BaristaVisibilityAssertions.assertDisplayed(R.id.playerName, "ALBERT")

        Thread.sleep(delay)

        onView(withId(R.id.action_edit))
                .perform(click())

        Thread.sleep(delay)

        // 3. Check back button come back on details
        pressBack()

        Thread.sleep(delay)

        //check name is albert
        BaristaVisibilityAssertions.assertDisplayed(R.id.playerName, "ALBERT")

        Thread.sleep(delay)

        //click on delete action button
        onView(withId(R.id.action_delete))
                .perform(click())

        Thread.sleep(delay)

        takeScreenshot("albert_delete_dialog", mHomeTestRule.activity)

        //confirm delete with dialog ok button
        BaristaClickInteractions.clickOn(R.id.confirm_button)

        Thread.sleep(delay)

        //click on the new first player Bernard
        onView(withId(R.id.teamPlayersRecyclerView))
                .perform(
                        RecyclerViewActions.actionOnItemAtPosition<TeamAdapter.PlayerViewHolder>(0, click())
                )

        Thread.sleep(delay)

        //check new first name is bernard
        BaristaVisibilityAssertions.assertDisplayed(R.id.playerName, "BERNARD")

        Thread.sleep(delay)

        //click on edit action button
        onView(withId(R.id.action_edit))
                .perform(click())

        Thread.sleep(delay)

        applyRotation("bernard_edit")

        Thread.sleep(delay)

        //go back to bernard details
        val appCompatImageButton2 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton2.perform(click())

        Thread.sleep(delay)

        //slide in pager to estelle
        BaristaViewPagerInteractions.swipeViewPagerForward()
        Thread.sleep(300)
        BaristaViewPagerInteractions.swipeViewPagerForward()
        Thread.sleep(300)
        BaristaViewPagerInteractions.swipeViewPagerForward()

        Thread.sleep(delay)

        val textView4 = onView(
                allOf(withId(R.id.playerName), withText("ESTELLE"),
                        childAtPosition(
                                allOf(withId(R.id.playerInformation),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.view.ViewGroup::class.java),
                                                0)),
                                1),
                        isDisplayed()))
        textView4.check(matches(withText("ESTELLE")))

        Thread.sleep(delay)

        //go back to list players
        val appCompatImageButton3 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton3.perform(click())

        Thread.sleep(delay)

        //add new player button clicked
        BaristaClickInteractions.clickOn(R.id.fab)

        Thread.sleep(delay)

        takeScreenshot("add_new_player", mHomeTestRule.activity)

        //fill name with NewPlayer
        BaristaEditTextInteractions.writeTo(R.id.playerNameInput, "NewPlayer")

        //fill shirt number with 42
        BaristaEditTextInteractions.writeTo(R.id.playerShirtNumberInput, "42")

        //fill license number with 2
        BaristaEditTextInteractions.writeTo(R.id.playerLicenseNumberInput, "42")

        //click on favorite positions
        BaristaClickInteractions.clickOn("LF")
        BaristaClickInteractions.clickOn("CF")
        BaristaClickInteractions.clickOn("RF")

        Thread.sleep(delay)

        //save and go back to the list of players
        BaristaClickInteractions.clickOn("Save")

        Thread.sleep(delay)

        //TODO check new player is at the end of the list

        Thread.sleep(delay)

        //go back to the dashboard
        val appCompatImageButton4 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton4.perform(click())

        Thread.sleep(delay)

        //click on the tile team size
        onView(withId(R.id.tileRecyclerView))
                .perform(
                        RecyclerViewActions.actionOnItemAtPosition<DashboardTileAdapter.TileViewHolder>(1, click())
                )

        Thread.sleep(delay)

        //check title Team Roster
        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
                .check(matches(withText("Team Roster")))

        Thread.sleep(delay)

        //go back to dashboard
        val appCompatImageButton5 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton5.perform(click())

        Thread.sleep(delay)

        //check now the most used player is Bernard
        onView(withId(R.id.tile_player_most_used_name)).check(matches(withText("BERNARD")))
    }

    @Test
    fun applyTeamManagementTests() {

        initialization()

        openDrawer()

        onView(allOf(withId(R.id.drawerImage), withParent(withId(R.id.navDrawerRootContainer))))
                .perform(click())

        Thread.sleep(delay)

        takeScreenshot("team_details", mHomeTestRule.activity)

        // check team name is "DC UNIVERS"
        BaristaVisibilityAssertions.assertContains(R.id.teamName, "DC UNIVERS")

        // check team type is undefined
        BaristaVisibilityAssertions.assertContains(R.id.teamTypeDescription, "Team type undefined")

        // check team size is 20
        BaristaVisibilityAssertions.assertContains(R.id.teamPlayersDescription, "Your team is composed of 20 members")

        // check team tournaments stats is 3T/8L
        BaristaVisibilityAssertions.assertContains(R.id.teamTournamentsDescription, "3 tournaments / 8 lineups")

        Thread.sleep(delay)

        //click on edit action button
        onView(withId(R.id.action_edit))
                .perform(click())

        Thread.sleep(delay)

        takeScreenshot("team_edit_name", mHomeTestRule.activity)

        applyRotation("team_edit_name")

        Thread.sleep(delay)

        //fill name with NewTeamName
        BaristaEditTextInteractions.writeTo(R.id.teamNameInput, "NewTeamName")

        Thread.sleep(delay)

        //click next
        BaristaClickInteractions.clickOn(R.id.buttonNext)

        Thread.sleep(delay)

        takeScreenshot("team_edit_team_type", mHomeTestRule.activity)

        //assert button name is Finish
        BaristaVisibilityAssertions.assertContains(R.id.buttonNext, "Finish")

        //choose softball type
        BaristaViewPagerInteractions.swipeViewPagerForward()

        Thread.sleep(delay)

        //finish edit flow
        BaristaClickInteractions.clickOn(R.id.buttonNext)

        Thread.sleep(delay)

        applyRotation("new_team_details")

        Thread.sleep(delay)

        // check team new name is "NewTeamName"
        BaristaVisibilityAssertions.assertContains(R.id.teamName, "NewTeamName")

        // check team type is softball
        BaristaVisibilityAssertions.assertContains(R.id.teamTypeDescription, "Softball team")

        // check team size is 21
        BaristaVisibilityAssertions.assertContains(R.id.teamPlayersDescription, "Your team is composed of 20 members")

        // check team tournaments stats is 3T/8L
        BaristaVisibilityAssertions.assertContains(R.id.teamTournamentsDescription, "3 tournaments / 8 lineups")

        Thread.sleep(delay)

        // click back on top left button
        val appCompatImageButton7 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton7.perform(click())

        Thread.sleep(delay)

        openDrawer()

        Thread.sleep(delay)

        //check team name is "NewTeamName"
        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.navDrawerRootContainer))))
                .check(matches(withText("NewTeamName")))
    }

    @Test
    fun applyLineupManagementTests() {

        initialization()

        openDrawer()

        //go to the lineups list screen
        onView(withId(R.id.nav_view))
                .perform(
                        NavigationViewActions.navigateTo(R.id.navigation_lineups)
                )

        Thread.sleep(delay)

        takeScreenshot("lineups_list", mHomeTestRule.activity)

        //click on stats tournament of the first tournament
        val appCompatImageButton9 = onView(allOf(
                withId(R.id.statsTournament),
                hasSibling(withText("Paris Series")),
                isDisplayed()))
        appCompatImageButton9.perform(click())

        Thread.sleep(delay)

        takeScreenshot("lineups_list_tournament_stats", mHomeTestRule.activity)

        applyRotation("lineups_list_tournament_stats")

        Thread.sleep(delay)

        // go back to the list of tournaments
        val appCompatImageButton10 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton10.perform(click())

        Thread.sleep(delay)

        applyRotation("lineups_list")

        Thread.sleep(delay)

        //click on the first lineup
        val constraintLayout = onView(
                allOf(withId(R.id.rootView),
                        isDescendantOfA(withId(R.id.lineupsOfTournamentRecycler)),
                        hasDescendant(withText("DC vs DC 1")),
                        isDisplayed()))
        constraintLayout.perform(click())

        Thread.sleep(delay)

        takeScreenshot("lineup_defense_fixed", mHomeTestRule.activity)

        //click on attack
        onView(allOf(withText("ATTACK"), isDescendantOfA(withId(R.id.lineupTabLayout))))
                .perform(click())

        Thread.sleep(delay)

        takeScreenshot("lineup_attack_fixed", mHomeTestRule.activity)

        //click on defense
        onView(allOf(withText("DEFENSE"), isDescendantOfA(withId(R.id.lineupTabLayout))))
                .perform(click())

        Thread.sleep(delay)

        applyRotation("lineup_defense_fixed")

        Thread.sleep(delay)

        //click on edit button
        onView(withId(R.id.action_edit))
                .perform(click())

        Thread.sleep(delay)

        takeScreenshot("lineup_defense_editable", mHomeTestRule.activity)

        //click on attack
        onView(allOf(withText("ATTACK"), isDescendantOfA(withId(R.id.lineupTabLayout))))
                .perform(click())

        takeScreenshot("lineup_attack_editable", mHomeTestRule.activity)

        Thread.sleep(delay)

        //click on defense
        onView(allOf(withText("DEFENSE"), isDescendantOfA(withId(R.id.lineupTabLayout))))
                .perform(click())

        Thread.sleep(delay)

        applyRotation("lineup_defense_editable")

        Thread.sleep(delay)

        //go back to the lineup fixed
        val appCompatImageButton11 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton11.perform(click())

        Thread.sleep(delay)

        //click on edit button
        onView(withId(R.id.action_delete))
                .perform(click())

        Thread.sleep(delay)

        //confirm delete with dialog ok button
        BaristaClickInteractions.clickOn(R.id.confirm_button)

        Thread.sleep(delay)

        BaristaVisibilityAssertions.assertDisplayed(R.id.headerText, "3 TOURNAMENTS / 7 LINEUPS")

        //click on delete tournament

        onView(allOf(withId(R.id.deleteTournament),
                hasSibling(withText("PARIS SERIES")),
                isDisplayed())).perform(click())

        Thread.sleep(delay)

        takeScreenshot("lineup_delete_popup", mHomeTestRule.activity)

        //confirm delete with dialog ok button
        BaristaClickInteractions.clickOn(R.id.confirm_button)

        Thread.sleep(delay)

        //check new summary header
        BaristaVisibilityAssertions.assertDisplayed(R.id.headerText, "2 TOURNAMENTS / 3 LINEUPS")

        //click on add lineup
        BaristaClickInteractions.clickOn(R.id.fab)

        Thread.sleep(delay)

        //hide the tap ta view
        val tapTargetView2 = onView(
                allOf(withClassName(`is`("com.getkeepsafe.taptargetview.TapTargetView")), isDisplayed()))
        tapTargetView2.perform(click())

        Thread.sleep(delay)

        BaristaEditTextInteractions.writeTo(R.id.lineupTitleInput, "NewLineup")
        BaristaEditTextInteractions.writeTo(R.id.tournamentChoiceAutoComplete, "NewTournament")

        Thread.sleep(delay)

        takeScreenshot("lineups_create_lineup", mHomeTestRule.activity)

        //TODO think something to perform click on calendar view

//        //open date expendable
//        val constraintLayout2 = onView(
//                allOf(withId(R.id.dateExpandableButton),
//                        childAtPosition(
//                                allOf(withId(R.id.dateExpandableContainer),
//                                        childAtPosition(
//                                                withClassName(`is`("androidx.appcompat.widget.LinearLayoutCompat")),
//                                                2)),
//                                0)))
//        constraintLayout2.perform(scrollTo(), click())
//
//        Thread.sleep(delay)
//
//
//        //choose 01/01/2020
//
//        onView(allOf(withContentDescription("Previous month"),
//                isDisplayed())).perform(click())
//
//        Thread.sleep(delay)
//
//        val appCompatImageButton13 = onView(
//                allOf(withClassName(`is`("androidx.appcompat.widget.AppCompatImageButton")), withContentDescription("Previous month"),
//                        childAtPosition(
//                                allOf(withId(R.id.calendarView),
//                                        childAtPosition(
//                                                withId(R.id.calendarView),
//                                                0)),
//                                1),
//                        isDisplayed()))
//        appCompatImageButton13.perform(click())
//
//        Thread.sleep(delay)

        //save the new lineup
        BaristaClickInteractions.clickOn(R.id.save)

        Thread.sleep(delay)

        //go to the list of lineups
        pressBack()

        Thread.sleep(delay)

        //check header
        BaristaVisibilityAssertions.assertDisplayed(R.id.headerText, "3 TOURNAMENTS / 4 LINEUPS")

//        onView(allOf(withId(R.id.tournamentDate),
//                hasSibling(withText("NewTournament")),
//                isDisplayed())).check(matches(withText("01/01/2020")))
    }

    @Test
    fun applySwitchTeamManagementTests() {

        initialization()

        openDrawer()

        //click on switch team
        BaristaClickInteractions.clickOn(R.id.changeTeam)

        Thread.sleep(delay)

        takeScreenshot("swap_team_dialog", mHomeTestRule.activity)

        //click on create
        BaristaClickInteractions.clickOn(android.R.id.button1)

        Thread.sleep(delay)

        applyRotation("create_team")

        Thread.sleep(delay)

        //call the new team toto
        BaristaEditTextInteractions.writeTo(R.id.teamNameInput, "toto")

        //click next again
        BaristaClickInteractions.clickOn(R.id.buttonNext)

        Thread.sleep(delay)

        //assert button name is Finish
        BaristaVisibilityAssertions.assertContains(R.id.buttonNext, "Finish")
        //click finish
        BaristaClickInteractions.clickOn(R.id.buttonNext)

        Thread.sleep(delay)

        openDrawer()

        Thread.sleep(delay)

        //click on image in drawer header
        onView(allOf(withId(R.id.drawerImage), withParent(withId(R.id.navDrawerRootContainer))))
                .perform(click())

        Thread.sleep(delay)

        // check team name is "TOTO"
        BaristaVisibilityAssertions.assertContains(R.id.teamName, "TOTO")

        // check team type is baseball
        BaristaVisibilityAssertions.assertContains(R.id.teamTypeDescription, "Baseball team")

        // check team size is 20
        BaristaVisibilityAssertions.assertContains(R.id.teamPlayersDescription, "Your team is composed of 0 members")

        // check team tournaments stats is 3T/8L
        BaristaVisibilityAssertions.assertContains(R.id.teamTournamentsDescription, "0 tournaments / 0 lineups")

        Thread.sleep(delay)

        pressBack()

        Thread.sleep(delay)

        openDrawer()

        Thread.sleep(delay)

        BaristaClickInteractions.clickOn(R.id.changeTeam)

        Thread.sleep(delay)

        //check first entry is NewTeam
        val textView20 = onView(
                allOf(withId(R.id.name),
                        withText("DC Univers"),
                        childAtPosition(
                                allOf(withId(R.id.details_container),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java),
                                                1)),
                                0),
                        isDisplayed()))
        textView20.check(matches(withText("DC Univers")))

//        val textView21 = onView(
//                allOf(withId(R.id.name), withText("toto"),
//                        childAtPosition(
//                                allOf(withId(R.id.details_container),
//                                        childAtPosition(
//                                                IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java),
//                                                1)),
//                                0),
//                        isDisplayed()))
//        textView21.check(matches(withText("toto")))
    }

    private fun applyRotation(name: String) {

        takeScreenshot("${name}_portrait", mHomeTestRule.activity)

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.setOrientationLeft()

        Thread.sleep(2000)

        takeScreenshot("${name}_landscape", mHomeTestRule.activity)

        device.setOrientationNatural()

        Thread.sleep(2000)
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
