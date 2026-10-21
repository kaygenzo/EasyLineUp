/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.painterResource
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
import com.telen.easylineup.compose.dashedBorder
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.getStrategies
import com.telen.easylineup.utils.getStrategiesDisplayName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LineupCreationState(
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
                icon = R.drawable.ic_close_24dp,
                onIconClick = onBackClick,
                iconContentDescription = stringResource(R.string.generic_close),
                eyebrow = stringResource(R.string.lineup_creation_header_eyebrow),
                title = stringResource(R.string.lineup_creation_header_title)
            )
            Spacer(modifier = Modifier.height(26.dp))

            FormLabel(stringResource(R.string.dialog_create_lineup_name), required = true)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                placeholder = { Text(stringResource(R.string.lineup_creation_name_placeholder)) },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = state.nameError?.let {
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
                value = state.selectedTournament?.name,
                placeholder = stringResource(R.string.lineup_creation_tournament_placeholder),
                subLabel = state.selectedTournament?.address,
                isError = state.tournamentError != null,
                errorText = state.tournamentError,
                onClick = onTournamentClick
            )
            Spacer(modifier = Modifier.height(20.dp))

            val dateEnabled = state.selectedTournament != null
            SelectField(
                label = stringResource(R.string.form_lineup_creation_date_label),
                value = if (dateEnabled) {
                    SimpleDateFormat("EEE, MMM d", Locale.getDefault())
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
            Spacer(modifier = Modifier.height(20.dp))

            val rosterSummary = stringResource(
                R.string.lineup_creation_roster_selected_format,
                state.roster.players.count { it.status },
                state.roster.players.size
            )
            SelectField(
                label = stringResource(R.string.form_lineup_creation_roster_label),
                value = rosterSummary,
                placeholder = rosterSummary,
                subLabel = null,
                onClick = onRosterClick
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (strategyNames != null) {
                val strategies = state.teamType.getStrategies()
                SegmentedRow(
                    label = stringResource(R.string.form_lineup_creation_strategy_label),
                    options = strategyNames.toList(),
                    selectedIndex = strategies.indexOf(state.strategy).coerceAtLeast(0),
                    onSelect = { index -> onStrategySelected(strategies[index]) }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            val extraHittersLabels = stringArrayResource(R.array.extra_hitters_labels)
            val extraHittersValues = integerArrayResource(R.array.extra_hitters_values)
            SegmentedRow(
                label = stringResource(R.string.form_lineup_creation_extra_hitters_label),
                options = extraHittersLabels.toList(),
                selectedIndex = extraHittersValues.indexOf(state.extraHitters).coerceAtLeast(0),
                onSelect = { index -> onExtraHittersSelected(extraHittersValues[index]) }
            )
            Spacer(modifier = Modifier.height(20.dp))
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
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        if (required) {
            Text(
                text = " *",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        FormLabel(label, required)
        Spacer(modifier = Modifier.height(6.dp))
        val borderColor = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
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
                    style = MaterialTheme.typography.bodyLarge
                )
                subLabel?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
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
                style = MaterialTheme.typography.bodySmall,
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
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEachIndexed { index, text ->
                SegmentOption(
                    text = text,
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .let { if (selected) it else it.border(1.dp, borderColor, RoundedCornerShape(8.dp)) }
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
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
                Text(
                    text = cancelLabel,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = saveLabel,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentPickerSheet(
    tournaments: List<Tournament>,
    selectedTournamentId: Long?,
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.lineup_creation_tournament_sheet_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            DugoutSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(
                    R.string.lineup_creation_tournament_search_placeholder
                ),
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(items = filtered, key = { it.id }) { tournament ->
                    TournamentPickerRow(
                        tournament = tournament,
                        isSelected = tournament.id == selectedTournamentId,
                        onClick = { onTournamentSelected(tournament); onDismiss() }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
private fun TournamentPickerRow(
    tournament: Tournament,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MiniDateChip(tournament.startTime.takeIf { it > 0 } ?: tournament.createdAt)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tournament.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            tournament.address?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (isSelected) {
            SelectedPill()
        }
    }
}

@Composable
private fun MiniDateChip(startMillis: Long) {
    val date = Date(startMillis)
    Column(
        modifier = Modifier
            .width(44.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = SimpleDateFormat("d", Locale.getDefault()).format(date),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = SimpleDateFormat("MMM", Locale.getDefault()).format(date).uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun SelectedPill() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.lineup_creation_tournament_selected_pill),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun CreateTournamentRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .dashedBorder(MaterialTheme.colorScheme.outline, cornerRadius = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = stringResource(R.string.lineup_creation_tournament_create_row),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterPickerSheet(
    roster: TeamRosterSummary,
    teamType: TeamType,
    onDismiss: () -> Unit,
    onPlayerToggled: (Int, Boolean) -> Unit,
    onClearAll: () -> Unit
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
    val summary = stringResource(
        R.string.lineup_creation_roster_selected_format,
        roster.players.count { it.status },
        roster.players.size
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.form_lineup_creation_roster_label),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = stringResource(R.string.lineup_creation_roster_clear_all),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onClearAll)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            DugoutSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(R.string.lineup_creation_roster_search_placeholder),
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                itemsIndexed(
                    items = filtered,
                    key = { _, indexed -> indexed.index }
                ) { position, (index, rosterPlayer) ->
                    RosterPickerRow(
                        player = rosterPlayer.player,
                        teamType = teamType,
                        checked = rosterPlayer.status,
                        onCheckedChange = { checked -> onPlayerToggled(index, checked) }
                    )
                    if (position != filtered.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.lineup_creation_roster_done)} · $summary",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun RosterPickerRow(
    player: Player,
    teamType: TeamType,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val positionLabel = primaryPositionLabel(player.positions, teamType)
    val statusText = if (checked) {
        stringResource(R.string.lineup_creation_roster_status_default, player.shirtNumber)
    } else {
        stringResource(R.string.lineup_creation_roster_status_sitting_out)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (checked) 1f else 0.45f)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RosterCheckbox(checked = checked, onCheckedChange = onCheckedChange)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "%02d".format(player.shirtNumber),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.titleSmall
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = if (positionLabel != null) "$positionLabel · $statusText" else statusText,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RosterCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val borderColor = if (checked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    }
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(1.5.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                painter = painterResource(R.drawable.ic_check_green_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun primaryPositionLabel(positions: Int, teamType: TeamType): String? {
    if (positions == 0) return null
    val names = if (teamType == TeamType.SOFTBALL) {
        stringArrayResource(R.array.field_positions_softball_list)
    } else {
        stringArrayResource(R.array.field_positions_baseball_list)
    }
    val match = FieldPosition.values().firstOrNull {
        it.id != 0 && it.id != 16 && (positions and it.mask) != 0
    } ?: return null
    return names.getOrNull(match.id)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTournamentSheet(
    name: String,
    startTimeInMillis: Long,
    endTimeInMillis: Long,
    address: String,
    onNameChanged: (String) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onAddressChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Text(
                    text = stringResource(R.string.lineup_creation_new_tournament_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.lineup_creation_new_tournament_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                FormLabel(
                    stringResource(R.string.lineup_creation_new_tournament_name_label),
                    required = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp),
                    placeholder = {
                        Text(
                            stringResource(R.string.lineup_creation_new_tournament_name_placeholder)
                        )
                    },
                    singleLine = true,
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

                val dateText = { millis: Long ->
                    SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(millis))
                }
                val pickDatePlaceholder =
                    stringResource(R.string.lineup_creation_new_tournament_pick_date)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SelectField(
                        label = stringResource(
                            R.string.lineup_creation_new_tournament_starts_label
                        ),
                        value = dateText(startTimeInMillis),
                        placeholder = pickDatePlaceholder,
                        subLabel = null,
                        modifier = Modifier.weight(1f),
                        onClick = onStartDateClick
                    )
                    SelectField(
                        label = stringResource(
                            R.string.lineup_creation_new_tournament_ends_label
                        ),
                        value = dateText(endTimeInMillis),
                        placeholder = pickDatePlaceholder,
                        subLabel = null,
                        modifier = Modifier.weight(1f),
                        onClick = onEndDateClick
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                FormLabel(stringResource(R.string.lineup_creation_new_tournament_location_label))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp),
                    placeholder = {
                        Text(
                            stringResource(
                                R.string.lineup_creation_new_tournament_location_placeholder
                            )
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            FormBar(
                cancelLabel = stringResource(R.string.cancel_button),
                saveLabel = stringResource(R.string.lineup_creation_new_tournament_save),
                onCancel = onDismiss,
                onSave = onAddClick
            )
        }
    }
}

@DugoutPreviews
@Composable
private fun LineupCreationScreenPreview() {
    DugoutTheme {
        LineupCreationScreen(
            state = LineupCreationState(
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
