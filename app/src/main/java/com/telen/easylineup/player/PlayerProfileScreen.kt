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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutCard
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.InfoCell
import com.telen.easylineup.compose.PlayerPhotoTile
import com.telen.easylineup.compose.dashedBorder
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
fun PlayerProfileScreen(state: PlayerProfileState, onStrategySelected: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
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
                        strategy = state.strategy,
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

@Composable
private fun PositionUsageChart(
    positionsData: Map<FieldPosition, Int>,
    strategy: TeamStrategy,
    teamType: TeamType
) {
    val context = LocalContext.current
    val positionNames = StringResourcesProviderImpl(context).positionShortNames(teamType.id)
    val maxCount = positionsData.values.maxOrNull()?.takeIf { it > 0 } ?: 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        strategy.positions.forEach { position ->
            val count = positionsData[position] ?: 0
            val isHighlighted = count > 0 && count == maxCount
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (count > 0) count.toString() else "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction = count / maxCount.toFloat())
                            .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                            .background(
                                if (isHighlighted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = positionNames.getOrNull(position.id) ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun PositionUsageEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .dashedBorder(MaterialTheme.colorScheme.outline, cornerRadius = 12.dp)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.player_profile_empty_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.player_profile_empty_message),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
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
            onStrategySelected = {}
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
            onStrategySelected = {}
        )
    }
}
