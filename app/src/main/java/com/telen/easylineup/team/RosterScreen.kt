/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team

import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.dashedBorder
import com.telen.easylineup.compose.rememberDugoutAvatarBitmap
import com.telen.easylineup.domain.model.PlayerSide

data class RosterRowState(
    val id: Long,
    val name: String,
    val shirtNumber: Int,
    val imageUri: Uri?,
    val battingSide: PlayerSide?,
    val pitchingSide: PlayerSide?
)

data class RosterState(
    val players: List<RosterRowState>,
    val sortType: TeamViewModel.SortType
)

@Composable
fun RosterScreen(
    state: RosterState,
    onSortSelected: (TeamViewModel.SortType) -> Unit,
    onPlayerClick: (Long) -> Unit,
    onAddPlayerClick: () -> Unit
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
        PlayerPhotoTile(imageUri = row.imageUri, shirtNumber = row.shirtNumber)

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
private fun PlayerPhotoTile(imageUri: Uri?, shirtNumber: Int) {
    val size = 50.dp
    val bitmap = rememberDugoutAvatarBitmap(imageUri, size)
    val cornerRadius = RoundedCornerShape(size * 0.29f)
    // The badge is a sibling of the clipped photo box, not a child of it, and pushed outward
    // via offset - matches the mockup's player-photo__num, which sits at bottom:-6px;right:-6px,
    // overflowing the tile rather than being cropped by its rounded corners.
    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(cornerRadius)
                .let {
                    if (bitmap == null) {
                        it.dashedBorder(
                            MaterialTheme.colorScheme.outline,
                            cornerRadius = size * 0.29f
                        )
                    } else {
                        it
                    }
                }
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    painter = BitmapPainter(bitmap),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_person_silhouette_24dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 6.dp, y = 6.dp)
                .size(23.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = shirtNumber.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
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

private fun PlayerSide.shortLabelRes(): Int = when (this) {
    PlayerSide.LEFT -> R.string.player_side_abbr_left
    PlayerSide.RIGHT -> R.string.player_side_abbr_right
    PlayerSide.BOTH -> R.string.player_side_abbr_both
}

@Composable
private fun PlayerSide.shortLabel(): String = stringResource(shortLabelRes())

@DugoutPreviews
@Composable
private fun RosterScreenPreview() {
    DugoutTheme {
        RosterScreen(
            state = RosterState(
                players = listOf(
                    RosterRowState(
                        id = 1,
                        name = "Jonas Alvarez",
                        shirtNumber = 7,
                        imageUri = null,
                        battingSide = PlayerSide.RIGHT,
                        pitchingSide = PlayerSide.RIGHT
                    ),
                    RosterRowState(
                        id = 2,
                        name = "Marcus Bell",
                        shirtNumber = 14,
                        imageUri = null,
                        battingSide = PlayerSide.LEFT,
                        pitchingSide = null
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
            onSortSelected = {},
            onPlayerClick = {},
            onAddPlayerClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun RosterScreenEmptyPreview() {
    DugoutTheme {
        RosterScreen(
            state = RosterState(players = emptyList(), sortType = TeamViewModel.SortType.ALPHA),
            onSortSelected = {},
            onPlayerClick = {},
            onAddPlayerClick = {}
        )
    }
}
