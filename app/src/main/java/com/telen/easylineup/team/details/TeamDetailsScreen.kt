/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.details

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutAvatar
import com.telen.easylineup.compose.DugoutCard
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.HeaderActionIconChip
import com.telen.easylineup.compose.InfoCell
import com.telen.easylineup.compose.ScreenHeader
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.utils.titleRes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TeamDetailsState(
    val teamName: String,
    val teamType: TeamType,
    val teamImageUri: Uri?,
    val rosterSize: Int,
    val lineupsBuilt: Int,
    val tournamentsCount: Int,
    val maleCount: Int,
    val femaleCount: Int,
    val unsetCount: Int,
    val createdAt: Long
)

private fun formatCreatedAt(createdAt: Long): String =
    if (createdAt <= 0L) "—" else SimpleDateFormat("MMM yyyy", Locale.getDefault())
        .format(Date(createdAt))

@Composable
fun TeamDetailsScreen(
    state: TeamDetailsState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenHeader(
            icon = R.drawable.ic_arrow_left_white_24dp,
            onIconClick = onBackClick,
            iconContentDescription = stringResource(R.string.generic_back),
            eyebrow = stringResource(R.string.title_settings),
            title = stringResource(R.string.team_details_header_title),
            modifier = Modifier.padding(bottom = 20.dp),
            actions = {
                HeaderActionIconChip(
                    icon = R.drawable.ic_edit_outline_24dp,
                    contentDescription = stringResource(R.string.action_edit),
                    onClick = onEditClick
                )
            }
        )

        ProfileHead(
            teamName = state.teamName,
            teamType = state.teamType,
            teamImageUri = state.teamImageUri
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoCell(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.team_details_roster_label),
                    value = pluralStringResource(
                        R.plurals.team_details_roster_count,
                        state.rosterSize,
                        state.rosterSize
                    )
                )
                InfoCell(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.team_details_lineups_label),
                    value = state.lineupsBuilt.toString()
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoCell(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.team_details_tournaments_label),
                    value = state.tournamentsCount.toString()
                )
                InfoCell(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.team_details_created_label),
                    value = formatCreatedAt(state.createdAt),
                    tabular = false
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DugoutCard {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = stringResource(R.string.team_details_roster_by_gender).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip(
                        modifier = Modifier.weight(1f),
                        value = state.maleCount,
                        label = stringResource(R.string.setting_men_style_description),
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        value = state.femaleCount,
                        label = stringResource(R.string.setting_women_style_description),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        value = state.unsetCount,
                        label = stringResource(R.string.team_details_stat_label_unset),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        DugoutCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDeleteClick)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.team_details_delete_title),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = stringResource(R.string.team_details_delete_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right_white_24dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileHead(teamName: String, teamType: TeamType, teamImageUri: Uri?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DugoutAvatar(name = teamName, imageUri = teamImageUri, size = 76.dp)
        Column {
            Text(
                text = teamName,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(5.dp))
            if (teamType.titleRes() != 0) {
                Pill(
                    text = stringResource(teamType.titleRes()),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun Pill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun StatChip(modifier: Modifier = Modifier, value: Int, label: String, color: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = value.toString(),
            color = color,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            fontSize = 19.sp
        )
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@DugoutPreviews
@Composable
private fun TeamDetailsScreenPreview() {
    DugoutTheme {
        TeamDetailsScreen(
            state = TeamDetailsState(
                teamName = "Riverside Hawks",
                teamType = TeamType.BASEBALL,
                teamImageUri = null,
                rosterSize = 14,
                lineupsBuilt = 23,
                tournamentsCount = 6,
                maleCount = 8,
                femaleCount = 5,
                unsetCount = 1,
                createdAt = 1709251200000L
            ),
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
