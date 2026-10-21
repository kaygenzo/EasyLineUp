/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.usecases.ImportResult

/**
 * How many teams/players/lineups/tournaments an export/import payload covers - shown before
 * either native file picker takes over, and again after, so the sheet's promise and the actual
 * result can be compared (mockup's `.info-grid`).
 */
data class DataCounts(val teams: Int, val players: Int, val lineups: Int, val tournaments: Int)

fun ExportBase.toDataCounts(): DataCounts = DataCounts(
    teams = teams.size,
    players = teams.sumOf { it.players.size },
    lineups = teams.sumOf { team -> team.tournaments.sumOf { it.lineups.size } },
    tournaments = teams.sumOf { it.tournaments.size }
)

@Composable
private fun DataCountsGrid(counts: DataCounts, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.data_transfer_count_teams),
                value = counts.teams.toString()
            )
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.data_transfer_count_players),
                value = counts.players.toString()
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.data_transfer_count_lineups),
                value = counts.lineups.toString()
            )
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.data_transfer_count_tournaments),
                value = counts.tournaments.toString()
            )
        }
    }
}

@Composable
private fun ImportResultGrid(result: ImportResult, modifier: Modifier = Modifier) {
    @Composable
    fun format(inserted: Int, updated: Int) =
        stringResource(R.string.data_transfer_added_updated, inserted, updated)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.data_transfer_count_teams),
                value = format(result.teams.inserted, result.teams.updated),
                tabular = false
            )
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.data_transfer_count_players),
                value = format(result.players.inserted, result.players.updated),
                tabular = false
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.data_transfer_count_lineups),
                value = format(result.lineups.inserted, result.lineups.updated),
                tabular = false
            )
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.data_transfer_count_tournaments),
                value = format(result.tournaments.inserted, result.tournaments.updated),
                tabular = false
            )
        }
    }
}

@Composable
private fun SheetButtonRow(
    cancelLabel: String?,
    confirmLabel: String,
    onCancel: (() -> Unit)?,
    onConfirm: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        if (cancelLabel != null && onCancel != null) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = cancelLabel, fontWeight = FontWeight.Bold)
            }
        }
        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = confirmLabel, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    sub: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    DugoutCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = sub,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportConfirmSheet(
    teamName: String,
    counts: DataCounts,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.export_confirm_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.export_confirm_message, teamName),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )
            DataCountsGrid(counts)
            Text(
                text = stringResource(R.string.export_confirm_footnote),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 10.dp, bottom = 18.dp)
            )
            SheetButtonRow(
                cancelLabel = stringResource(R.string.cancel_button),
                confirmLabel = stringResource(R.string.export_confirm_action),
                onCancel = onDismiss,
                onConfirm = onConfirm
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSuccessSheet(
    folderName: String?,
    fileName: String,
    counts: DataCounts,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.export_success_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (folderName != null) {
                    stringResource(R.string.export_success_message, folderName, fileName)
                } else {
                    stringResource(R.string.export_success_message_no_folder, fileName)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )
            DataCountsGrid(counts)
            Spacer(modifier = Modifier.height(14.dp))
            SheetButtonRow(
                cancelLabel = null,
                confirmLabel = stringResource(R.string.generic_done),
                onCancel = null,
                onConfirm = onDismiss
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportConfirmSheet(
    fileName: String,
    counts: DataCounts,
    overwriteExisting: Boolean,
    onOverwriteChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.import_confirm_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = fileName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )
            DataCountsGrid(counts)
            Spacer(modifier = Modifier.height(14.dp))
            ToggleRow(
                label = stringResource(R.string.import_confirm_overwrite_label),
                sub = stringResource(R.string.import_confirm_overwrite_sub),
                checked = overwriteExisting,
                onCheckedChange = onOverwriteChanged
            )
            Text(
                text = stringResource(R.string.import_confirm_footnote),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 10.dp, bottom = 18.dp)
            )
            SheetButtonRow(
                cancelLabel = stringResource(R.string.cancel_button),
                confirmLabel = stringResource(R.string.import_confirm_action),
                onCancel = onDismiss,
                onConfirm = onConfirm
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSuccessSheet(fileName: String, result: ImportResult, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.import_success_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = fileName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )
            ImportResultGrid(result)
            Spacer(modifier = Modifier.height(14.dp))
            SheetButtonRow(
                cancelLabel = null,
                confirmLabel = stringResource(R.string.generic_done),
                onCancel = null,
                onConfirm = onDismiss
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
