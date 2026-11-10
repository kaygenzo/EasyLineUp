/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import android.view.ContextThemeWrapper
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.OverrideRosterPlayerNumber
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Typing a temporary shirt number in the roster picker, one key at a time, the way a keyboard
 * does. Each keystroke reports a new override that comes back into the row; the field used to
 * rebuild its text from it mid-typing ("7" reformatted to "07", cursor moved), so the second
 * digit landed in the wrong place and got dropped.
 */
@RunWith(AndroidJUnit4::class)
class RosterNumberOverrideTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val overrideRosterPlayerNumber = OverrideRosterPlayerNumber()

    @Test
    fun typingTwoDigitsKeepsBoth() {
        var lastReported: Int? = null
        composeTestRule.setContent {
            // The rule's bare activity has no app theme, which DugoutTheme reads its colors from.
            val themed = ContextThemeWrapper(LocalContext.current, R.style.AppTheme)
            CompositionLocalProvider(LocalContext provides themed) {
                DugoutTheme {
                    var roster by remember {
                        mutableStateOf(
                            TeamRosterSummary(
                                status = Constants.STATUS_ALL,
                                players = listOf(
                                    RosterPlayerStatus(
                                        Player(1L, 1L, "Alex Rivera", PLAYER_NUMBER, "0"),
                                        true,
                                        null
                                    )
                                )
                            )
                        )
                    }
                    RosterPickerContent(
                        roster = roster,
                        teamType = TeamType.BASEBALL,
                        onPlayerToggled = { _, _ -> },
                        onClearAll = {},
                        onDoneClick = {},
                        onNumberChanged = { position, number ->
                            lastReported = number
                            roster = overrideRosterPlayerNumber(roster, position, number, 0L)
                        }
                    )
                }
            }
        }

        // The sheet's search field is the first editable node, the number field the second.
        val numberField = { composeTestRule.onAllNodes(hasSetTextAction())[1] }
        numberField().assertTextEquals("$PLAYER_NUMBER")
        numberField().performTextClearance()
        numberField().performTextInput("7")
        numberField().performTextInput("3")

        numberField().assertTextEquals("73")
        assertEquals(73, lastReported)
    }

    private companion object {
        const val PLAYER_NUMBER = 12
    }
}
