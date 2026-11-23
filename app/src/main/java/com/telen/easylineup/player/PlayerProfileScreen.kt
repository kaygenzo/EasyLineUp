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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import com.telen.easylineup.compose.TwoPaneRow
import com.telen.easylineup.compose.chipLabel
import com.telen.easylineup.compose.heroSubtitle
import com.telen.easylineup.compose.heroTitle
import com.telen.easylineup.compose.isWide
import com.telen.easylineup.compose.sectionLabel
import com.telen.easylineup.compose.shortLabel
import com.telen.easylineup.compose.uppercaseLocalized
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getStrategies
import com.telen.easylineup.domain.usecases.GetMostPlayedPosition
import com.telen.easylineup.utils.StringResourcesProviderImpl
import com.telen.easylineup.utils.getStrategiesDisplayName

data class PlayerProfileState(
    val name: String,
    val shirtNumber: Int,
    val imageUri: Uri?,
    val sex: Sex,
    val battingSide: PlayerSide?,
    val pitchingSide: PlayerSide?,
    val licenseNumber: String,
    val email: String,
    val phoneNumber: String,
    val positionsData: Map<FieldPosition, Int>,
    val teamType: TeamType,
    val strategy: TeamStrategy,
    val selectedStrategyIndex: Int
)

/**
 * A player's profile. With no [onBackClick], it's the detail pane of the roster's list-detail
 * (wide screens): no back arrow and no "Roster" title then, the list is right next to it, only
 * the edit and delete actions.
 */
@Composable
fun PlayerProfileScreen(
    state: PlayerProfileState,
    onStrategySelected: (Int) -> Unit,
    onBackClick: (() -> Unit)?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val strategyOptions = state.teamType.getStrategiesDisplayName(context)?.toList() ?: emptyList()

    val actions: @Composable RowScope.() -> Unit = {
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
    val header: @Composable (Modifier) -> Unit = { modifier ->
        if (onBackClick == null) {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                content = actions
            )
        } else {
            ScreenHeader(
                icon = R.drawable.ic_arrow_left_white_24dp,
                onIconClick = onBackClick,
                iconContentDescription = stringResource(R.string.generic_back),
                eyebrow = null,
                title = stringResource(R.string.title_team),
                modifier = modifier,
                actions = actions
            )
        }
    }
    // Who the player is, then how they've been used: stacked when tall, side by side when
    // wide, so the chart isn't below the fold of a landscape phone.
    val identity: @Composable ColumnScope.() -> Unit = {
        ProfileHeader(state)

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.player_hint_license_number_short),
                value = state.licenseNumber.ifBlank { EMPTY_MARKER }
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

    }
    val usage: @Composable ColumnScope.() -> Unit = {
        DugoutCard {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = stringResource(
                        R.string.player_profile_position_usage_label
                    ).uppercaseLocalized(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.sectionLabel
                )
                if (strategyOptions.size > 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    StrategyPillsRow(
                        options = strategyOptions,
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
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isWide) {
            Column(modifier = Modifier.fillMaxSize()) {
                header(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp))
                TwoPaneRow(
                    modifier = Modifier.weight(1f),
                    spacing = 0.dp,
                    first = identity,
                    second = usage
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                header(Modifier.padding(bottom = 20.dp))
                identity()
                Spacer(modifier = Modifier.height(12.dp))
                usage()
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
                    style = MaterialTheme.typography.heroTitle
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
                    style = MaterialTheme.typography.heroSubtitle,
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
                licenseNumber = "FR-22841",
                email = "jonas.a@mail.com",
                phoneNumber = "(555) 019-2231",
                positionsData = mapOf(FieldPosition.SHORT_STOP to 15),
                teamType = TeamType.BASEBALL,
                strategy = TeamType.BASEBALL.getStrategies()[0],
                selectedStrategyIndex = 0
            )
        )
    }
}

@Composable
private fun mostPlayedPositionLabel(
    positionsData: Map<FieldPosition, Int>,
    teamType: TeamType,
    getMostPlayedPosition: GetMostPlayedPosition = GetMostPlayedPosition()
): String? {
    val position = getMostPlayedPosition(positionsData) ?: return null
    val context = LocalContext.current
    val names = StringResourcesProviderImpl(context).positionShortNames(teamType.id)
    return names.getOrNull(position.id)
}

@DugoutPreviews
@Composable
private fun MostPlayedPositionLabelPreview() {
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
        Text(text = text, color = contentColor, style = MaterialTheme.typography.chipLabel)
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
                licenseNumber = "FR-22841",
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
private fun PlayerProfileScreenInPanePreview() {
    DugoutTheme {
        PlayerProfileScreen(
            state = PlayerProfileState(
                name = "Jonas Alvarez",
                shirtNumber = 7,
                imageUri = null,
                sex = Sex.MALE,
                battingSide = PlayerSide.RIGHT,
                pitchingSide = PlayerSide.RIGHT,
                licenseNumber = "FR-22841",
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
                selectedStrategyIndex = 0
            ),
            onStrategySelected = {},
            onBackClick = null,
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
                licenseNumber = "",
                email = "",
                phoneNumber = "",
                positionsData = emptyMap(),
                teamType = TeamType.SOFTBALL,
                strategy = TeamType.SOFTBALL.getStrategies()[0],
                selectedStrategyIndex = 0
            ),
            onStrategySelected = {},
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
