package com.telen.easylineup


import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import androidx.viewpager2.widget.ViewPager2
import com.adevinta.android.barista.assertion.BaristaCheckedAssertions
import com.adevinta.android.barista.assertion.BaristaListAssertions
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions
import com.adevinta.android.barista.interaction.*
import com.adevinta.android.barista.internal.assertAny
import com.adevinta.android.barista.internal.matcher.DisplayedMatchers
import com.adevinta.android.barista.rule.cleardata.ClearPreferencesRule
import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter
import com.google.android.material.chip.Chip
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.After
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
    var mHomeTestRule = EasyLineupActivityTestRule(
        HomeActivity::class.java,
        initialTouchMode = true,
        launchActivity = false
    )

    @Rule
    @JvmField
    var clearPreferencesRule = ClearPreferencesRule()

//    @Rule
////    @JvmField
////    var clearDatabaseRule = ClearDatabaseRule()

    @Before
    fun init() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        mHomeTestRule.launchActivity(Intent())
    }

    @After
    fun clear() {
    }

    private fun takeScreenshot(name: String, activity: Activity) {
        BaristaSleepInteractions.sleep(delay)
        val testLabSetting =
            Settings.System.getString(activity.contentResolver, "firebase.test.lab")
        if ("true" == testLabSetting) {
            ScreenShotter.takeScreenshot(name, activity)
        }
    }

    private fun applyRotation(name: String) {

        takeScreenshot("${name}_portrait", mHomeTestRule.activity)

        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())

        BaristaSleepInteractions.sleep(1000)

        takeScreenshot("${name}_landscape", mHomeTestRule.activity)

        onView(isRoot()).perform(OrientationChangeAction.orientationPortrait())

        BaristaSleepInteractions.sleep(2000)
    }

    private fun initialization() {
        takeScreenshot("dashboard", mHomeTestRule.activity)
        BaristaDrawerInteractions.openDrawer()
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

        applyRotation("players")

        //check sort by name is checked
        BaristaCheckedAssertions.assertChecked(R.id.sort_by_name)

        //click on sort by shirt number
        BaristaClickInteractions.clickOn(R.id.sort_by_shirt_number)

        //check sort by shirt number is checked
        BaristaCheckedAssertions.assertChecked(R.id.sort_by_shirt_number)

        //assert first player is damien
        BaristaListAssertions.assertDisplayedAtPosition(
            R.id.teamPlayersRecyclerView,
            0,
            R.id.playerName,
            "DAMIEN"
        )

        //click on sort by name
        BaristaClickInteractions.clickOn(R.id.sort_by_name)

        //assert first player is albert
        BaristaListAssertions.assertDisplayedAtPosition(
            R.id.teamPlayersRecyclerView,
            0,
            R.id.playerName,
            "ALBERT"
        )

        // Click on first player Albert
        BaristaListInteractions.clickListItem(R.id.teamPlayersRecyclerView, 0)

        takeScreenshot("albert_details", mHomeTestRule.activity)

        //check name is ALBERT
        BaristaVisibilityAssertions.assertDisplayed(R.id.playerName, "ALBERT")

        applyRotation("albert_details")

        //click on edit action button
        BaristaMenuClickInteractions.clickMenu(R.id.action_edit)

        takeScreenshot("albert_edit", mHomeTestRule.activity)

        applyRotation("albert_edit")

        // 1. Check save button come back on details
        BaristaClickInteractions.clickOn(R.id.playerSave)

        //check name is albert
        BaristaVisibilityAssertions.assertDisplayed(R.id.playerName, "ALBERT")

        BaristaMenuClickInteractions.clickMenu(R.id.action_edit)

        // 2. Check cancel button come back on details
        BaristaClickInteractions.clickOn(R.id.cancel)

        //confirm discard changes with dialog ok button
        BaristaDialogInteractions.clickDialogPositiveButton()

        //check name is albert
        BaristaVisibilityAssertions.assertDisplayed(R.id.playerName, "ALBERT")

        BaristaMenuClickInteractions.clickMenu(R.id.action_edit)

        // 3. Check back button come back on details
        pressBack()

        //confirm discard changes with dialog ok button
        BaristaDialogInteractions.clickDialogPositiveButton()

        //check name is albert
        BaristaVisibilityAssertions.assertDisplayed(R.id.playerName, "ALBERT")

        //click on delete action button
        BaristaMenuClickInteractions.clickMenu(R.id.action_delete)

        takeScreenshot("albert_delete_dialog", mHomeTestRule.activity)

        //confirm delete with dialog ok button
        BaristaDialogInteractions.clickDialogPositiveButton()

        //click on the new first player Bernard
        BaristaListInteractions.clickListItem(R.id.teamPlayersRecyclerView, 0)

        //check new first name is bernard
        BaristaVisibilityAssertions.assertDisplayed(R.id.playerName, "BERNARD")

        //click on edit action button
        BaristaMenuClickInteractions.clickMenu(R.id.action_edit)

        applyRotation("bernard_edit")

        //go back to bernard details
        clickNavigateUp()

        //slide in pager to estelle
        BaristaViewPagerInteractions.swipeViewPagerForward()
        BaristaViewPagerInteractions.swipeViewPagerForward()
        BaristaViewPagerInteractions.swipeViewPagerForward()

        // check the current player is Estelle
        BaristaVisibilityAssertions.assertDisplayed(R.id.playerName, "ESTELLE")

        //go back to list players
        clickNavigateUp()

        //add new player button clicked
        BaristaClickInteractions.clickOn(R.id.fab)

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

        //save and go back to the list of players
        BaristaClickInteractions.clickOn("Save")

        //add new player button clicked
        BaristaClickInteractions.clickOn(R.id.fab)

        //fill name with Aaa
        BaristaEditTextInteractions.writeTo(R.id.playerNameInput, "Aaa")

        //save and go back to the list of players
        BaristaClickInteractions.clickOn("Save")

        //assert first player is albert
        BaristaListAssertions.assertDisplayedAtPosition(
            R.id.teamPlayersRecyclerView,
            0,
            R.id.playerName,
            "AAA"
        )

        //go back to the dashboard
        clickNavigateUp()

        //click on the tile team size
        BaristaListInteractions.clickListItem(R.id.tileRecyclerView, 0)

        //check title Team Roster
        onView(allOf(instanceOf(AppCompatTextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText("Roster")))

        //go back to dashboard
        clickNavigateUp()

        //check now the most used player is Bernard
        BaristaListAssertions.assertDisplayedAtPosition(
            R.id.tileRecyclerView,
            1,
            R.id.tile_player_most_used_name,
            "BERNARD"
        )
    }

    @Test
    fun applyTeamManagementTests() {

        initialization()

        BaristaDrawerInteractions.openDrawer()

        BaristaClickInteractions.clickOn(R.id.drawerImage)

        takeScreenshot("team_details", mHomeTestRule.activity)

        //click on team image button to expand card
        BaristaClickInteractions.clickOn(R.id.teamTypeRepresentation)

        // check team name is "DC UNIVERS"
        BaristaVisibilityAssertions.assertContains(R.id.teamTypeTitle, "DC Univers")

        // check team size is 20
        BaristaVisibilityAssertions.assertContains("Your team is composed of 20 members")

        // check team tournaments stats is 3T/8L
        BaristaVisibilityAssertions.assertContains("3 tournaments / 8 lineups")

        //click on edit action button
        BaristaMenuClickInteractions.clickMenu(R.id.action_edit)

        takeScreenshot("team_edit_name", mHomeTestRule.activity)

        applyRotation("team_edit_name")

        //fill name with NewTeamName
        BaristaEditTextInteractions.writeTo(R.id.teamNameInput, "NewTeamName")

        //click next
        BaristaClickInteractions.clickOn(R.id.buttonNext)

        takeScreenshot("team_edit_team_type", mHomeTestRule.activity)

        //assert button name is Finish
        BaristaVisibilityAssertions.assertContains(R.id.buttonNext, "Finish")

        //choose softball type
        onView(DisplayedMatchers.displayedAssignableFrom(ViewPager2::class.java))
            .perform(swipeLeft())
        //finish edit flow
        BaristaClickInteractions.clickOn(R.id.buttonNext)

        applyRotation("new_team_details")

        //click on team image button to expand card
        BaristaClickInteractions.clickOn(R.id.teamTypeRepresentation)

        // check team new name is "NewTeamName"
        BaristaVisibilityAssertions.assertContains(R.id.teamTypeTitle, "NewTeamName")

        // check team size is 21
        BaristaVisibilityAssertions.assertContains("Your team is composed of 20 members")

        // check team tournaments stats is 3T/8L
        BaristaVisibilityAssertions.assertContains("3 tournaments / 8 lineups")

        // click back on top left button
        clickNavigateUp()

        BaristaDrawerInteractions.openDrawer()

        //check team name is "NewTeamName"
        BaristaVisibilityAssertions.assertContains(R.id.drawerTitle, "NewTeamName")
    }

    @Test
    fun applyLineupManagementTests() {

        initialization()

        BaristaDrawerInteractions.openDrawer()

        //go to the lineups list screen
        onView(withId(R.id.nav_view))
            .perform(
                NavigationViewActions.navigateTo(R.id.navigation_lineups)
            )

        takeScreenshot("lineups_list", mHomeTestRule.activity)

        //click on stats tournament of the first tournament
        BaristaListInteractions.clickListItemChild(R.id.recyclerView, 0, R.id.statsTournament)

        takeScreenshot("lineups_list_tournament_stats", mHomeTestRule.activity)

        applyRotation("lineups_list_tournament_stats")

        // go back to the list of tournaments
        clickNavigateUp()

        applyRotation("lineups_list")

        //click on the first lineup
        val constraintLayout = onView(
            allOf(
                withId(R.id.rootView),
                isDescendantOfA(withId(R.id.lineupsOfTournamentRecycler)),
                hasDescendant(withText("DC vs DC 1")),
                isDisplayed()
            )
        )
        constraintLayout.perform(click())

        takeScreenshot("lineup_defense_fixed", mHomeTestRule.activity)

        //click on attack
        BaristaClickInteractions.clickOn("ATTACK")

        takeScreenshot("lineup_attack_fixed", mHomeTestRule.activity)

        //click on defense
        BaristaClickInteractions.clickOn("DEFENSE")

        applyRotation("lineup_defense_fixed")

        //click on edit button
        BaristaMenuClickInteractions.clickMenu(R.id.action_edit)

        takeScreenshot("lineup_defense_editable", mHomeTestRule.activity)

        //click on attack
        BaristaClickInteractions.clickOn("ATTACK")

        takeScreenshot("lineup_attack_editable", mHomeTestRule.activity)

        //click on defense
        BaristaClickInteractions.clickOn("DEFENSE")

        applyRotation("lineup_defense_editable")

        //go back to the lineup fixed
        clickNavigateUp()

        //click on edit button
        BaristaMenuClickInteractions.clickMenu(R.id.action_delete)

        //confirm delete with dialog ok button
        BaristaDialogInteractions.clickDialogPositiveButton()

        BaristaListInteractions.scrollListToPosition(R.id.recyclerView, 0)

        //click on delete tournament
        BaristaListInteractions.clickListItemChild(R.id.recyclerView, 0, R.id.deleteTournament)

        takeScreenshot("lineup_delete_popup", mHomeTestRule.activity)

        //confirm delete with dialog ok button
        BaristaDialogInteractions.clickDialogPositiveButton()

        //click on add lineup
        BaristaClickInteractions.clickOn(R.id.fab)

        BaristaEditTextInteractions.writeTo(R.id.lineupTitleInput, "NewLineup")
        BaristaEditTextInteractions.writeTo(R.id.tournamentChoiceAutoComplete, "NewTournament")

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
//
//        //choose 01/01/2020
//
//        onView(allOf(withContentDescription("Previous month"),
//                isDisplayed())).perform(click())
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
        //save the new lineup
        BaristaClickInteractions.clickOn(R.id.save)

        //go to the list of lineups
        pressBack()

//        onView(allOf(withId(R.id.tournamentDate),
//                hasSibling(withText("NewTournament")),
//                isDisplayed())).check(matches(withText("01/01/2020")))
    }

    @Test
    fun applySwitchTeamManagementTests() {

        initialization()

        BaristaDrawerInteractions.openDrawer()

        //click on switch team
        BaristaClickInteractions.clickOn(R.id.changeTeam)

        takeScreenshot("swap_team_dialog", mHomeTestRule.activity)

        //click on create
        BaristaDialogInteractions.clickDialogPositiveButton()

        applyRotation("create_team")

        //call the new team toto
        BaristaEditTextInteractions.writeTo(R.id.teamNameInput, "toto")

        // open dialog and clicking cancel button on dialog
        BaristaClickInteractions.clickOn(R.id.buttonPrevious)
        BaristaDialogInteractions.clickDialogNegativeButton()

        // change screen and come back
        BaristaClickInteractions.clickOn(R.id.buttonNext)
        BaristaClickInteractions.clickOn(R.id.buttonPrevious)

        // open dialog and close it by press physical back button
        pressBack()
        pressBack()

        //click next again
        BaristaClickInteractions.clickOn(R.id.buttonNext)

        //assert button name is Finish
        BaristaVisibilityAssertions.assertContains(R.id.buttonNext, "Finish")
        //click finish
        BaristaClickInteractions.clickOn(R.id.buttonNext)

        BaristaDrawerInteractions.openDrawer()

        //click on image in drawer header
        BaristaClickInteractions.clickOn(R.id.drawerImage)

        //click on team image button to expand card
        onView(withId(R.id.teamTypeRepresentation))
            .perform(click())

        // check team name is "TOTO"
        BaristaVisibilityAssertions.assertContains(R.id.teamTypeTitle, "toto")

        // check team size is 0
        BaristaVisibilityAssertions.assertContains("Your team is composed of 0 members")

        // check team tournaments stats is 0T/0L
        BaristaVisibilityAssertions.assertContains("0 tournaments / 0 lineups")

        pressBack()

        BaristaDrawerInteractions.openDrawer()

        BaristaClickInteractions.clickOn(R.id.changeTeam)

        //check first entry is DC Univers
        BaristaListAssertions.assertDisplayedAtPosition(R.id.list, 0, R.id.name, "DC Univers")

        //check second entry is toto
        BaristaListAssertions.assertDisplayedAtPosition(R.id.list, 1, R.id.name, "toto")
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

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

    fun withMenuIdOrText(@IdRes id: Int, @StringRes menuText: Int): Matcher<View?>? {
        val matcher = withId(id)
        return try {
            onView(matcher).check(matches(isDisplayed()))
            matcher
        } catch (NoMatchingViewException: Exception) {
            openActionBarOverflowOrOptionsMenu(
                InstrumentationRegistry.getInstrumentation().targetContext
            )
            withText(menuText)
        }
    }

    fun clickNavigateUp() {
        val appCompatImageButton10 = onView(
            allOf(
                withContentDescription("Navigate up"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(
                                `is`("com.google.android.material.appbar.AppBarLayout")
                            ),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton10.perform(click())
    }
}
