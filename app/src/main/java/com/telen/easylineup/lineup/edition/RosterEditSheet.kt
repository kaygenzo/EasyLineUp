/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.lineup.create.RosterPickerContent

/**
 * Standalone roster-selection sheet - the same [RosterPickerContent] the "Edit lineup"/"Create
 * lineup" sheets use for their roster step, but reachable directly (no name/tournament fields in
 * front of it), for screens where roster membership is the only thing being edited.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterEditSheet(
    roster: TeamRosterSummary,
    teamType: TeamType,
    onDismiss: () -> Unit,
    onPlayerToggled: (Int, Boolean) -> Unit,
    onNumberChanged: (Int, Int) -> Unit,
    onClearAll: () -> Unit,
    onDoneClick: () -> Unit
) {
    // Same dismiss-gating as EditLineupSheet: intercept the scrim-tap/back auto-hide so a dirty
    // check can decide whether to show a discard-confirm instead of letting the sheet vanish.
    val currentOnDismiss = rememberUpdatedState(onDismiss)
    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = {
            if (it == SheetValue.Hidden) {
                currentOnDismiss.value()
                false
            } else {
                true
            }
        }
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        RosterPickerContent(
            roster = roster,
            teamType = teamType,
            onPlayerToggled = onPlayerToggled,
            onClearAll = onClearAll,
            onDoneClick = onDoneClick,
            onNumberChanged = onNumberChanged
        )
    }
}

private val RosterEditSheetPreviewRoster = TeamRosterSummary(
    status = 1,
    players = listOf(
        RosterPlayerStatus(Player(1L, 1L, "Alex Rivera", 12, ""), true, null),
        RosterPlayerStatus(Player(2L, 1L, "Sam Cole", 7, ""), false, null)
    )
)

@DugoutPreviews
@Composable
private fun RosterEditSheetPreview() {
    DugoutTheme {
        RosterEditSheet(
            roster = RosterEditSheetPreviewRoster,
            teamType = TeamType.BASEBALL,
            onDismiss = {},
            onPlayerToggled = { _, _ -> },
            onNumberChanged = { _, _ -> },
            onClearAll = {},
            onDoneClick = {}
        )
    }
}
