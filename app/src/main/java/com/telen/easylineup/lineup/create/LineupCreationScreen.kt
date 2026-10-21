/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutSearchField
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.ScreenHeader
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.getStrategies
import com.telen.easylineup.utils.getStrategiesDisplayName
import java.text.DateFormat
import java.util.Date

data class LineupCreationState(
    val teamName: String,
    val teamType: TeamType,
    val name: String,
    val nameError: String?,
    val tournaments: List<Tournament>,
    val selectedTournament: Tournament?,
    val tournamentError: String?,
    val eventTimeInMillis: Long,
    val roster: TeamRosterSummary,
    val strategy: TeamStrategy,
    val extraHitters: Int
)

@Composable
fun LineupCreationScreen(
    state: LineupCreationState,
    onBackClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onTournamentClick: () -> Unit,
    onDateClick: () -> Unit,
    onRosterClick: () -> Unit,
    onStrategySelected: (TeamStrategy) -> Unit,
    onExtraHittersSelected: (Int) -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current
    val strategyNames = state.teamType.getStrategiesDisplayName(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ScreenHeader(
                icon = R.drawable.ic_arrow_left_white_24dp,
                onIconClick = onBackClick,
                iconContentDescription = stringResource(R.string.generic_back),
                eyebrow = state.teamName,
                title = stringResource(R.string.lineup_creation_header_title)
            )
            Spacer(modifier = Modifier.height(18.dp))

            FormLabel(stringResource(R.string.dialog_create_lineup_name), required = true)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.lineup_creation_name_placeholder)) },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))

            SelectField(
                label = stringResource(R.string.dialog_create_lineup_tournament),
                required = true,
                value = state.selectedTournament?.name,
                placeholder = stringResource(R.string.lineup_creation_tournament_placeholder),
                subLabel = state.selectedTournament?.address,
                isError = state.tournamentError != null,
                errorText = state.tournamentError,
                onClick = onTournamentClick
            )
            Spacer(modifier = Modifier.height(14.dp))

            val dateEnabled = state.selectedTournament != null
            SelectField(
                label = stringResource(R.string.form_lineup_creation_date_label),
                value = if (dateEnabled) {
                    DateFormat.getDateInstance(DateFormat.SHORT)
                        .format(Date(state.eventTimeInMillis))
                } else {
                    null
                },
                placeholder = stringResource(R.string.lineup_creation_date_disabled_hint),
                subLabel = state.selectedTournament?.takeIf { it.startTime > 0 && it.endTime > 0 }
                    ?.let { stringResource(R.string.lineup_creation_date_sub_within, it.name) },
                enabled = dateEnabled,
                onClick = onDateClick
            )
            Spacer(modifier = Modifier.height(14.dp))

            val rosterSummary = if (state.roster.status == Constants.STATUS_ALL) {
                stringResource(R.string.roster_size_status_all)
            } else {
                val selected = state.roster.players.count { it.status }
                pluralStringResource(R.plurals.roster_size_status_selection, selected, selected)
            }
            SelectField(
                label = stringResource(R.string.form_lineup_creation_roster_label),
                value = rosterSummary,
                placeholder = rosterSummary,
                subLabel = null,
                onClick = onRosterClick
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (strategyNames != null) {
                val strategies = state.teamType.getStrategies()
                SegmentedRow(
                    label = stringResource(R.string.form_lineup_creation_strategy_label),
                    options = strategyNames.toList(),
                    selectedIndex = strategies.indexOf(state.strategy).coerceAtLeast(0),
                    onSelect = { index -> onStrategySelected(strategies[index]) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            val extraHittersLabels = stringArrayResource(R.array.extra_hitters_labels)
            val extraHittersValues = integerArrayResource(R.array.extra_hitters_values)
            SegmentedRow(
                label = stringResource(R.string.form_lineup_creation_extra_hitters_label),
                options = extraHittersLabels.toList(),
                selectedIndex = extraHittersValues.indexOf(state.extraHitters).coerceAtLeast(0),
                onSelect = { index -> onExtraHittersSelected(extraHittersValues[index]) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        FormBar(
            cancelLabel = stringResource(R.string.cancel_button),
            saveLabel = stringResource(R.string.lineup_creation_save),
            onCancel = onCancelClick,
            onSave = onSaveClick
        )
    }
}

@Composable
private fun FormLabel(text: String, required: Boolean = false) {
    Row {
        Text(
            text = text.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        if (required) {
            Text(
                text = " *",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SelectField(
    label: String,
    value: String?,
    placeholder: String,
    subLabel: String?,
    required: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    onClick: () -> Unit
) {
    Column {
        FormLabel(label, required)
        Spacer(modifier = Modifier.height(4.dp))
        val borderColor = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.45f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value ?: placeholder,
                    color = if (value != null) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (value != null) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyMedium
                )
                subLabel?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_down_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(-90f)
            )
        }
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SegmentedRow(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column {
        FormLabel(label)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEachIndexed { index, text ->
                SegmentOption(
                    text = text,
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SegmentOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FormBar(
    cancelLabel: String,
    saveLabel: String,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = cancelLabel, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = saveLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentPickerSheet(
    tournaments: List<Tournament>,
    onDismiss: () -> Unit,
    onTournamentSelected: (Tournament) -> Unit,
    onCreateTournamentClick: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, tournaments) {
        if (query.isBlank()) {
            tournaments
        } else {
            tournaments.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.dialog_create_lineup_tournament),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            DugoutSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(
                    R.string.lineup_creation_tournament_search_placeholder
                ),
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(items = filtered, key = { it.id }) { tournament ->
                    TournamentPickerRow(
                        tournament = tournament,
                        onClick = { onTournamentSelected(tournament); onDismiss() }
                    )
                }
                item {
                    CreateTournamentRow(onClick = onCreateTournamentClick)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TournamentPickerRow(tournament: Tournament, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tournament.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            tournament.address?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CreateTournamentRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add_white_24dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(R.string.lineup_creation_tournament_create_row),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterPickerSheet(
    roster: TeamRosterSummary,
    onDismiss: () -> Unit,
    onPlayerToggled: (Int, Boolean) -> Unit,
    onToggleAll: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val indexedPlayers = remember(roster) { roster.players.withIndex().toList() }
    val filtered = remember(query, indexedPlayers) {
        if (query.isBlank()) {
            indexedPlayers
        } else {
            indexedPlayers.filter { it.value.player.name.contains(query, ignoreCase = true) }
        }
    }
    val summary = if (roster.status == Constants.STATUS_ALL) {
        stringResource(R.string.roster_size_status_all)
    } else {
        val selected = roster.players.count { it.status }
        pluralStringResource(R.plurals.roster_size_status_selection, selected, selected)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.form_lineup_creation_roster_label),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            DugoutSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(R.string.lineup_creation_roster_search_placeholder),
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleAll)
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.lineup_creation_roster_toggle_all),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                items(items = filtered, key = { it.index }) { (index, rosterPlayer) ->
                    RosterPickerRow(
                        name = rosterPlayer.player.name,
                        shirtNumber = rosterPlayer.player.shirtNumber,
                        checked = rosterPlayer.status,
                        onCheckedChange = { checked -> onPlayerToggled(index, checked) }
                    )
                }
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.lineup_creation_roster_done)} · $summary",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RosterPickerRow(
    name: String,
    shirtNumber: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = name,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "#$shirtNumber",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@DugoutPreviews
@Composable
private fun LineupCreationScreenPreview() {
    DugoutTheme {
        LineupCreationScreen(
            state = LineupCreationState(
                teamName = "Riverside Hawks",
                teamType = TeamType.BASEBALL,
                name = "Game 3 — Starters",
                nameError = null,
                tournaments = listOf(
                    Tournament(
                        id = 1L,
                        name = "Fall Classic",
                        createdAt = 1760000000000L,
                        startTime = 1760000000000L,
                        endTime = 1760003600000L,
                        address = "Riverside Park"
                    )
                ),
                selectedTournament = Tournament(
                    id = 1L,
                    name = "Fall Classic",
                    createdAt = 1760000000000L,
                    startTime = 1760000000000L,
                    endTime = 1760003600000L,
                    address = "Riverside Park"
                ),
                tournamentError = null,
                eventTimeInMillis = 1760000000000L,
                roster = TeamRosterSummary(
                    Constants.STATUS_PARTIAL,
                    listOf(
                        RosterPlayerStatus(
                            Player(1L, 1L, "Alex Rivera", 12, 0L),
                            true,
                            null
                        ),
                        RosterPlayerStatus(
                            Player(2L, 1L, "Sam Cole", 7, 0L),
                            false,
                            null
                        )
                    )
                ),
                strategy = TeamStrategy.STANDARD,
                extraHitters = 1
            ),
            onBackClick = {},
            onNameChanged = {},
            onTournamentClick = {},
            onDateClick = {},
            onRosterClick = {},
            onStrategySelected = {},
            onExtraHittersSelected = {},
            onCancelClick = {},
            onSaveClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun LineupCreationScreenEmptyPreview() {
    DugoutTheme {
        LineupCreationScreen(
            state = LineupCreationState(
                teamName = "Riverside Hawks",
                teamType = TeamType.BASEBALL,
                name = "",
                nameError = null,
                tournaments = emptyList(),
                selectedTournament = null,
                tournamentError = null,
                eventTimeInMillis = System.currentTimeMillis(),
                roster = TeamRosterSummary(Constants.STATUS_ALL, emptyList()),
                strategy = TeamStrategy.STANDARD,
                extraHitters = 0
            ),
            onBackClick = {},
            onNameChanged = {},
            onTournamentClick = {},
            onDateClick = {},
            onRosterClick = {},
            onStrategySelected = {},
            onExtraHittersSelected = {},
            onCancelClick = {},
            onSaveClick = {}
        )
    }
}
