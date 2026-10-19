/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.HeaderActionIconChip
import com.telen.easylineup.compose.PlayerPhotoTile
import com.telen.easylineup.compose.ScreenHeader
import com.telen.easylineup.compose.dashedBorder
import com.telen.easylineup.compose.shortLabel
import com.telen.easylineup.domain.model.PlayerSide

data class RosterRowState(
    val id: Long,
    val name: String,
    val shirtNumber: Int,
    val imageUri: Uri?,
    val battingSide: PlayerSide?,
    val pitchingSide: PlayerSide?,
    val phone: String? = null,
    val email: String? = null
)

data class RosterState(
    val teamName: String,
    val players: List<RosterRowState>,
    val sortType: TeamViewModel.SortType
)

@Composable
fun RosterScreen(
    state: RosterState,
    onBackClick: () -> Unit,
    onSortSelected: (TeamViewModel.SortType) -> Unit,
    onPlayerClick: (Long) -> Unit,
    onAddPlayerClick: () -> Unit,
    onMessageTeamClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            ScreenHeader(
                icon = R.drawable.ic_arrow_left_white_24dp,
                onIconClick = onBackClick,
                iconContentDescription = stringResource(R.string.generic_back),
                eyebrow = state.teamName,
                title = stringResource(R.string.title_team),
                modifier = Modifier.padding(bottom = 14.dp),
                actions = if (state.players.isNotEmpty()) {
                    {
                        HeaderActionIconChip(
                            icon = R.drawable.ic_welcome_message,
                            contentDescription = stringResource(
                                R.string.roster_message_team_content_description
                            ),
                            onClick = onMessageTeamClick
                        )
                    }
                } else {
                    null
                }
            )

            SortPillsRow(selected = state.sortType, onSortSelected = onSortSelected)

            Spacer(modifier = Modifier.height(14.dp))

            if (state.players.isEmpty()) {
                RosterEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    itemsIndexed(
                        items = state.players,
                        key = { _, player -> player.id }
                    ) { index, player ->
                        PlayerRow(row = player, onClick = { onPlayerClick(player.id) })
                        if (index < state.players.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddPlayerClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_white_24dp),
                contentDescription = stringResource(R.string.roster_add_player_content_description)
            )
        }
    }
}

@Composable
private fun SortPillsRow(
    selected: TeamViewModel.SortType,
    onSortSelected: (TeamViewModel.SortType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SortPill(
            text = stringResource(R.string.roster_sort_by_name),
            selected = selected == TeamViewModel.SortType.ALPHA,
            onClick = { onSortSelected(TeamViewModel.SortType.ALPHA) }
        )
        SortPill(
            text = stringResource(R.string.roster_sort_by_number),
            selected = selected == TeamViewModel.SortType.NUMERIC,
            onClick = { onSortSelected(TeamViewModel.SortType.NUMERIC) }
        )
    }
}

@Composable
private fun SortPill(text: String, selected: Boolean, onClick: () -> Unit) {
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .let {
                if (selected) {
                    it
                } else {
                    it.border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(50)
                    )
                }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = contentColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun PlayerRow(row: RosterRowState, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        PlayerPhotoTile(imageUri = row.imageUri, shirtNumber = row.shirtNumber, size = 50.dp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleSmall
            )
            val subtitle = listOfNotNull(
                row.battingSide?.let {
                    stringResource(R.string.roster_player_bats, it.shortLabel())
                },
                row.pitchingSide?.let {
                    stringResource(R.string.roster_player_throws, it.shortLabel())
                }
            ).joinToString(" · ")
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun RosterEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .dashedBorder(MaterialTheme.colorScheme.outline, cornerRadius = 16.dp)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_person_silhouette_24dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.roster_empty_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.roster_empty_message),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageTeamSheet(
    teamName: String,
    players: List<RosterRowState>,
    onDismiss: () -> Unit,
    onTextMessageClick: () -> Unit,
    onEmailClick: () -> Unit,
    onShareAnotherWayClick: () -> Unit
) {
    val phoneCount = players.count { !it.phone.isNullOrBlank() }
    val emailCount = players.count { !it.email.isNullOrBlank() }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Text(
                text = stringResource(R.string.roster_message_sheet_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = teamName + " · " + pluralStringResource(
                    R.plurals.team_details_roster_count,
                    players.size,
                    players.size
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )
            MessageSheetRow(
                icon = R.drawable.ic_welcome_message,
                iconTint = MaterialTheme.colorScheme.primary,
                label = stringResource(R.string.roster_message_text_label),
                caption = stringResource(
                    R.string.roster_message_phone_count,
                    phoneCount,
                    players.size
                ),
                onClick = { onDismiss(); onTextMessageClick() }
            )
            MessageSheetRow(
                icon = R.drawable.ic_baseline_email_24,
                iconTint = MaterialTheme.colorScheme.secondary,
                label = stringResource(R.string.roster_message_email_label),
                caption = stringResource(
                    R.string.roster_message_email_count,
                    emailCount,
                    players.size
                ),
                onClick = { onDismiss(); onEmailClick() }
            )
            MessageSheetRow(
                icon = R.drawable.ic_share_white_24dp,
                iconTint = MaterialTheme.colorScheme.tertiary,
                label = stringResource(R.string.roster_message_share_label),
                caption = stringResource(R.string.roster_message_share_caption),
                showDivider = false,
                onClick = { onDismiss(); onShareAnotherWayClick() }
            )
            Text(
                text = stringResource(R.string.roster_message_footnote),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 10.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun MessageSheetRow(
    icon: Int,
    iconTint: Color,
    label: String,
    caption: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right_white_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    }
}

@DugoutPreviews
@Composable
private fun RosterScreenPreview() {
    DugoutTheme {
        RosterScreen(
            state = RosterState(
                teamName = "Riverside Hawks",
                players = listOf(
                    RosterRowState(
                        id = 1,
                        name = "Jonas Alvarez",
                        shirtNumber = 7,
                        imageUri = null,
                        battingSide = PlayerSide.RIGHT,
                        pitchingSide = PlayerSide.RIGHT,
                        phone = "555-0101",
                        email = "jonas@example.com"
                    ),
                    RosterRowState(
                        id = 2,
                        name = "Marcus Bell",
                        shirtNumber = 14,
                        imageUri = null,
                        battingSide = PlayerSide.LEFT,
                        pitchingSide = null,
                        phone = "555-0102"
                    ),
                    RosterRowState(
                        id = 3,
                        name = "Devon Ruiz",
                        shirtNumber = 23,
                        imageUri = null,
                        battingSide = null,
                        pitchingSide = null
                    )
                ),
                sortType = TeamViewModel.SortType.ALPHA
            ),
            onBackClick = {},
            onSortSelected = {},
            onPlayerClick = {},
            onAddPlayerClick = {},
            onMessageTeamClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun RosterScreenEmptyPreview() {
    DugoutTheme {
        RosterScreen(
            state = RosterState(
                teamName = "Riverside Hawks",
                players = emptyList(),
                sortType = TeamViewModel.SortType.ALPHA
            ),
            onBackClick = {},
            onSortSelected = {},
            onPlayerClick = {},
            onAddPlayerClick = {},
            onMessageTeamClick = {}
        )
    }
}
