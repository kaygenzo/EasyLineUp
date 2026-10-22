/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.FormBar
import com.telen.easylineup.compose.FormLabel
import com.telen.easylineup.compose.HeaderIconChip
import com.telen.easylineup.compose.SelectField
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.lineup.create.RosterPickerContent

enum class EditLineupStep { FIELDS, ROSTER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLineupSheet(
    name: String,
    nameError: String?,
    selectedTournament: Tournament?,
    tournamentError: String?,
    roster: TeamRosterSummary,
    step: EditLineupStep,
    teamType: TeamType,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onTournamentClick: () -> Unit,
    onRosterClick: () -> Unit,
    onRosterBackClick: () -> Unit,
    onPlayerToggled: (Int, Boolean) -> Unit,
    onNumberChanged: (Int, Int) -> Unit,
    onClearAll: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    // Material3 hides the sheet internally as soon as the scrim is tapped or back
    // is pressed, before onDismissRequest even runs - intercepting the value change
    // here is the documented way to gate that on the dirty-check in onDismiss
    // instead, rather than letting the sheet vanish while a confirm sheet decides.
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
        when (step) {
            EditLineupStep.FIELDS -> EditLineupFieldsContent(
                name = name,
                nameError = nameError,
                selectedTournament = selectedTournament,
                tournamentError = tournamentError,
                roster = roster,
                onNameChanged = onNameChanged,
                onTournamentClick = onTournamentClick,
                onRosterClick = onRosterClick,
                onCancelClick = onCancelClick,
                onSaveClick = onSaveClick
            )

            EditLineupStep.ROSTER -> EditLineupRosterContent(
                roster = roster,
                teamType = teamType,
                onBackClick = onRosterBackClick,
                onPlayerToggled = onPlayerToggled,
                onNumberChanged = onNumberChanged,
                onClearAll = onClearAll
            )
        }
    }
}

@Composable
private fun EditLineupFieldsContent(
    name: String,
    nameError: String?,
    selectedTournament: Tournament?,
    tournamentError: String?,
    roster: TeamRosterSummary,
    onNameChanged: (String) -> Unit,
    onTournamentClick: () -> Unit,
    onRosterClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            Text(
                text = stringResource(R.string.edit_lineup_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = selectedTournament?.name.orEmpty(),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            FormLabel(
                stringResource(R.string.dialog_create_lineup_name),
                required = true
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                placeholder = { Text(stringResource(R.string.lineup_creation_name_placeholder)) },
                singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let {
                    { Text(it, style = MaterialTheme.typography.bodySmall) }
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            SelectField(
                label = stringResource(R.string.dialog_create_lineup_tournament),
                required = true,
                value = selectedTournament?.name,
                placeholder = stringResource(R.string.lineup_creation_tournament_placeholder),
                subLabel = selectedTournament?.address,
                isError = tournamentError != null,
                errorText = tournamentError,
                onClick = onTournamentClick
            )
            Spacer(modifier = Modifier.height(20.dp))

            val rosterSummary = stringResource(
                R.string.lineup_creation_roster_selected_format,
                roster.players.count { it.status },
                roster.players.size
            )
            SelectField(
                label = stringResource(R.string.form_lineup_creation_roster_label),
                value = rosterSummary,
                placeholder = rosterSummary,
                subLabel = null,
                onClick = onRosterClick
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.edit_lineup_footnote),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        FormBar(
            cancelLabel = stringResource(R.string.cancel_button),
            saveLabel = stringResource(R.string.edit_lineup_save_changes),
            onCancel = onCancelClick,
            onSave = onSaveClick
        )
    }
}

@Composable
private fun EditLineupRosterContent(
    roster: TeamRosterSummary,
    teamType: TeamType,
    onBackClick: () -> Unit,
    onPlayerToggled: (Int, Boolean) -> Unit,
    onNumberChanged: (Int, Int) -> Unit,
    onClearAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderIconChip(
                icon = R.drawable.ic_arrow_left_white_24dp,
                contentDescription = stringResource(R.string.generic_back),
                onClick = onBackClick
            )
        }
        RosterPickerContent(
            roster = roster,
            teamType = teamType,
            onPlayerToggled = onPlayerToggled,
            onClearAll = onClearAll,
            onDoneClick = onBackClick,
            onNumberChanged = onNumberChanged
        )
    }
}
