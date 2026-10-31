/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutCard
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.HeaderActionIconChip
import com.telen.easylineup.compose.InfoCell
import com.telen.easylineup.compose.PlayerPhotoTile
import com.telen.easylineup.compose.PositionUsageChart
import com.telen.easylineup.compose.PositionUsageEmptyState
import com.telen.easylineup.compose.ScreenHeader
import com.telen.easylineup.compose.shortLabel
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getStrategies
import com.telen.easylineup.utils.StringResourcesProviderImpl

data class PlayerProfileState(
    val name: String,
    val shirtNumber: Int,
    val imageUri: Uri?,
    val sex: Sex,
    val battingSide: PlayerSide?,
    val pitchingSide: PlayerSide?,
    val licenseNumber: Long,
    val email: String,
    val phoneNumber: String,
    val positionsData: Map<FieldPosition, Int>,
    val teamType: TeamType,
    val strategy: TeamStrategy,
    val strategyOptions: List<String>,
    val selectedStrategyIndex: Int
)

@Composable
fun PlayerProfileScreen(
    state: PlayerProfileState,
    onStrategySelected: (Int) -> Unit,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        ScreenHeader(
            icon = R.drawable.ic_arrow_left_white_24dp,
            onIconClick = onBackClick,
            iconContentDescription = stringResource(R.string.generic_back),
            eyebrow = null,
            title = stringResource(R.string.title_team),
            modifier = Modifier.padding(bottom = 20.dp),
            actions = {
                HeaderActionIconChip(
                    icon = R.drawable.ic_edit_outline_24dp,
                    contentDescription = stringResource(R.string.action_edit),
                    onClick = onEditClick
                )
                Spacer(modifier = Modifier.width(8.dp))
                HeaderActionIconChip(
                    icon = R.drawable.ic_delete_outline_24dp,
                    contentDescription = stringResource(R.string.action_delete),
                    onClick = onDeleteClick
                )
            }
        )

        ProfileHeader(state)

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.player_hint_license_number_short),
                value = state.licenseNumber.takeIf { it > 0 }?.toString() ?: EMPTY_MARKER
            )
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.tile_most_used_player_games_played),
                value = state.positionsData.values.sum().toString()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.player_hint_email),
                value = state.email.ifEmpty { EMPTY_MARKER },
                tabular = false
            )
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.player_hint_phone),
                value = state.phoneNumber.ifEmpty { EMPTY_MARKER },
                tabular = false
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        DugoutCard {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = stringResource(R.string.player_profile_position_usage_label).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium
                )
                if (state.strategyOptions.size > 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    StrategyPillsRow(
                        options = state.strategyOptions,
                        selectedIndex = state.selectedStrategyIndex,
                        onSelected = onStrategySelected
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                if (state.positionsData.values.sum() == 0) {
                    PositionUsageEmptyState()
                } else {
                    PositionUsageChart(
                        positionsData = state.positionsData,
                        positions = state.strategy.positions.toList(),
                        teamType = state.teamType
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(state: PlayerProfileState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PlayerPhotoTile(imageUri = state.imageUri, shirtNumber = state.shirtNumber, size = 84.dp)
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = state.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge
                )
                val sexIconRes = when (state.sex) {
                    Sex.MALE -> R.drawable.ic_male_black
                    Sex.FEMALE -> R.drawable.ic_female_black
                    Sex.UNKNOWN -> null
                }
                if (sexIconRes != null) {
                    Icon(
                        painter = painterResource(sexIconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            val mostPlayedLabel = mostPlayedPositionLabel(state.positionsData, state.teamType)
            val sides = listOfNotNull(
                state.battingSide?.let {
                    stringResource(R.string.roster_player_bats, it.shortLabel())
                },
                state.pitchingSide?.let {
                    stringResource(R.string.roster_player_throws, it.shortLabel())
                }
            ).joinToString(" / ")
            val subtitle = listOfNotNull(mostPlayedLabel, sides.takeIf { it.isNotEmpty() })
                .joinToString(" · ")
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@DugoutPreviews
@Composable
private fun ProfileHeaderPreview() {
    DugoutTheme {
        ProfileHeader(
            state = PlayerProfileState(
                name = "Jonas Alvarez",
                shirtNumber = 7,
                imageUri = null,
                sex = Sex.MALE,
                battingSide = PlayerSide.RIGHT,
                pitchingSide = PlayerSide.RIGHT,
                licenseNumber = 22841,
                email = "jonas.a@mail.com",
                phoneNumber = "(555) 019-2231",
                positionsData = mapOf(FieldPosition.SHORT_STOP to 15),
                teamType = TeamType.BASEBALL,
                strategy = TeamType.BASEBALL.getStrategies()[0],
                strategyOptions = listOf("Standard"),
                selectedStrategyIndex = 0
            )
        )
    }
}

@Composable
private fun mostPlayedPositionLabel(
    positionsData: Map<FieldPosition, Int>,
    teamType: TeamType
): String? {
    val topEntry = positionsData.maxByOrNull { it.value } ?: return null
    if (topEntry.value <= 0) return null
    val context = LocalContext.current
    val names = StringResourcesProviderImpl(context).positionShortNames(teamType.id)
    return names.getOrNull(topEntry.key.id)
}

@DugoutPreviews
@Composable
private fun mostPlayedPositionLabelPreview() {
    DugoutTheme {
        Text(
            text = mostPlayedPositionLabel(
                positionsData = mapOf(FieldPosition.SHORT_STOP to 15),
                teamType = TeamType.BASEBALL
            ) ?: ""
        )
    }
}

@Composable
private fun StrategyPillsRow(options: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            StrategyPill(
                text = label,
                selected = index == selectedIndex,
                onClick = { onSelected(index) }
            )
        }
    }
}

@DugoutPreviews
@Composable
private fun StrategyPillsRowPreview() {
    DugoutTheme {
        StrategyPillsRow(
            options = listOf("Standard", "Slowpitch"),
            selectedIndex = 0,
            onSelected = {}
        )
    }
}

@Composable
private fun StrategyPill(text: String, selected: Boolean, onClick: () -> Unit) {
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
        Text(text = text, color = contentColor, style = MaterialTheme.typography.labelSmall)
    }
}

@DugoutPreviews
@Composable
private fun StrategyPillPreview() {
    DugoutTheme {
        StrategyPill(text = "Standard", selected = true, onClick = {})
    }
}

internal const val EMPTY_MARKER = "-"

@DugoutPreviews
@Composable
private fun PlayerProfileScreenPreview() {
    DugoutTheme {
        PlayerProfileScreen(
            state = PlayerProfileState(
                name = "Jonas Alvarez",
                shirtNumber = 7,
                imageUri = null,
                sex = Sex.MALE,
                battingSide = PlayerSide.RIGHT,
                pitchingSide = PlayerSide.RIGHT,
                licenseNumber = 22841,
                email = "jonas.a@mail.com",
                phoneNumber = "(555) 019-2231",
                positionsData = mapOf(
                    FieldPosition.SHORT_STOP to 15,
                    FieldPosition.SECOND_BASE to 6,
                    FieldPosition.THIRD_BASE to 3,
                    FieldPosition.DP_DH to 2,
                    FieldPosition.CENTER_FIELD to 1
                ),
                teamType = TeamType.BASEBALL,
                strategy = TeamType.BASEBALL.getStrategies()[0],
                strategyOptions = listOf("Standard"),
                selectedStrategyIndex = 0
            ),
            onStrategySelected = {},
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun PlayerProfileScreenEmptyPreview() {
    DugoutTheme {
        PlayerProfileScreen(
            state = PlayerProfileState(
                name = "Marcus Bell",
                shirtNumber = 14,
                imageUri = null,
                sex = Sex.UNKNOWN,
                battingSide = null,
                pitchingSide = null,
                licenseNumber = 0,
                email = "",
                phoneNumber = "",
                positionsData = emptyMap(),
                teamType = TeamType.SOFTBALL,
                strategy = TeamType.SOFTBALL.getStrategies()[0],
                strategyOptions = listOf("Standard", "Slowpitch"),
                selectedStrategyIndex = 0
            ),
            onStrategySelected = {},
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
