/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Scale
import coil3.toBitmap
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getStrategies
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.utils.StringResourcesProviderImpl

/**
 * Dashed outline for the Dugout Card system's recurring "nothing here yet" language (an empty
 * player photo, an open field position, an "add team/tournament" row - all share this same
 * treatment instead of each screen inventing its own empty state).
 */
fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.5.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp
): Modifier = drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()))
    )
    val inset = strokeWidth.toPx() / 2
    drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - inset * 2, size.height - inset * 2),
        cornerRadius = CornerRadius(cornerRadius.toPx() - inset),
        style = stroke
    )
}

/**
 * Shared flat card container for the Dugout Card design system - rounded corners, no elevation,
 * surface color, 1dp outline border (matches the mockup's `.card { border: 1px solid var(--line) }`
 * - elevation alone doesn't read as a boundary on a flat card). Used by every screen that groups
 * rows/content into a card (Settings, Team Details, ...). Extracted once a second screen needed
 * the same chrome, instead of re-copying a private composable.
 */
@Composable
fun DugoutCard(
    borderWidth: Dp = 1.dp,
    borderAlpha: Float = 0.6f,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            borderWidth,
            MaterialTheme.colorScheme.outline.copy(alpha = borderAlpha)
        )
    ) {
        Column { content() }
    }
}

@DugoutPreviews
@Composable
private fun DugoutCardPreview() {
    DugoutTheme {
        DugoutCard {
            Text(text = "Sample card content", modifier = Modifier.padding(16.dp))
        }
    }
}

/**
 * Shared flat search field - panel-2 background, no border, matching the mockup's
 * `.search-field` (and the ticket's own date-chip background). Extracted once a second screen
 * (a bottom-sheet picker) needed the exact same chrome as the Tournament list's own search box.
 */
@Composable
fun DugoutSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_baseline_search_24),
                contentDescription = null
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent
        )
    )
}

@DugoutPreviews
@Composable
private fun DugoutSearchFieldPreview() {
    DugoutTheme {
        DugoutSearchField(
            value = "Hawks",
            onValueChange = {},
            placeholder = "Search teams"
        )
    }
}

/**
 * Shared avatar for anything identified by a name and an optional photo (teams today, players
 * tomorrow): shows the Coil-loaded photo when [imageUri] is set, otherwise a rounded tile with
 * the name's initials - same fallback language as the rest of the Dugout Card system.
 */
@Composable
fun DugoutAvatar(name: String, imageUri: Uri?, size: Dp) {
    val bitmap = rememberDugoutAvatarBitmap(imageUri, size)
    // size * 0.29 matches the mockup's own ratio (22px radius / 76px box, 12px / 40px box) -
    // RoundedCornerShape needs a Dp here, not size.value (a bare Float is pixels, not dp).
    val cornerRadius = RoundedCornerShape(size * 0.29f)
    Box(
        modifier = Modifier
            .size(size)
            .clip(cornerRadius)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), cornerRadius),
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
            Text(
                text = name.avatarInitials(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (size.value * 0.33f).sp
            )
        }
    }
}

@DugoutPreviews
@Composable
private fun DugoutAvatarPreview() {
    DugoutTheme {
        DugoutAvatar(name = "John Doe", imageUri = null, size = 56.dp)
    }
}

private fun String.avatarInitials(): String =
    trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

/**
 * Coil-backed bitmap loader shared by every Compose screen that displays a user-picked photo
 * (team/player avatars today) - not private, so a screen needing a different wrapper than
 * [DugoutAvatar] (e.g. an empty/dashed picker state) can still reuse the same loading logic.
 */
@Composable
fun rememberDugoutAvatarBitmap(uri: Uri?, size: Dp): ImageBitmap? {
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val context = LocalContext.current

    LaunchedEffect(uri, sizePx) {
        bitmap = null
        if (uri == null) {
            return@LaunchedEffect
        }
        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(sizePx, sizePx)
            .scale(Scale.FILL)
            .allowHardware(false)
            .build()
        bitmap = context.imageLoader.execute(request).image?.toBitmap()?.asImageBitmap()
    }
    return bitmap
}

@DugoutPreviews
@Composable
private fun RememberDugoutAvatarBitmapPreview() {
    DugoutTheme {
        val bitmap = rememberDugoutAvatarBitmap(uri = null, size = 56.dp)
        Text(text = if (bitmap != null) "Loaded" else "No image")
    }
}

/**
 * Photo-or-dashed-silhouette tile with a corner jersey-number badge, shared by anything that
 * shows a player's identity at a glance (Roster rows, Player profile's header) - same filled
 * tile or dashed placeholder either way, so identity reads consistently wherever a player shows
 * up. The badge size is derived from [size] (0.46 is exactly 23dp/50dp, Roster's original fixed
 * ratio) so existing call sites keep their exact look when this got promoted out of RosterScreen.
 */
@Composable
fun PlayerPhotoTile(imageUri: Uri?, shirtNumber: Int, size: Dp) {
    val bitmap = rememberDugoutAvatarBitmap(imageUri, size)
    val cornerRadius = RoundedCornerShape(size * 0.29f)
    val badgeSize = size * 0.46f
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
                    modifier = Modifier.size(size * 0.4f)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = size * 0.12f, y = size * 0.12f)
                .size(badgeSize)
                .clip(RoundedCornerShape(badgeSize * 0.3f))
                .background(MaterialTheme.colorScheme.primary)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(badgeSize * 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = shirtNumber.toString().padStart(2, '0'),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                fontSize = (badgeSize.value * 0.43f).sp
            )
        }
    }
}

@DugoutPreviews
@Composable
private fun PlayerPhotoTilePreview() {
    DugoutTheme {
        PlayerPhotoTile(imageUri = null, shirtNumber = 7, size = 64.dp)
    }
}

/**
 * Small dot + uppercase label pill marking a screen as mid-edit (Attack's "Editing order",
 * Defense's "Editing positions") - promoted here once a second screen needed the exact same
 * badge, just with different text.
 */
@Composable
fun ModeBadge(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@DugoutPreviews
@Composable
private fun ModeBadgePreview() {
    DugoutTheme {
        ModeBadge(label = "Editing order")
    }
}

/**
 * Label + bordered value box shared by any screen that groups raw stats into quiet "info cells"
 * (Team Details, Player profile) instead of competing plain data rows. [tabular] picks the bigger
 * monospace style for a number meant to be scanned at a glance; off for free-text values (an
 * email, a formatted date).
 */
@Composable
fun InfoCell(modifier: Modifier = Modifier, label: String, value: String, tabular: Boolean = true) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground,
            style = if (tabular) {
                MaterialTheme.typography.bodyLarge
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = FontWeight.Bold,
            fontFamily = if (tabular) FontFamily.Monospace else FontFamily.Default
        )
    }
}

@DugoutPreviews
@Composable
private fun InfoCellPreview() {
    DugoutTheme {
        InfoCell(label = "Games played", value = "128")
    }
}

/**
 * A single number-over-label chip, 3 side by side in a `.stat-row` (team roster's gender
 * breakdown, this screen's avg/low/high) - the mockup's `.stat-chip`.
 */
@Composable
fun StatChip(
    modifier: Modifier = Modifier,
    value: Int,
    label: String,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
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
private fun StatChipPreview() {
    DugoutTheme {
        StatChip(value = 12, label = "Avg games")
    }
}

/**
 * A per-position usage bar chart - one bar per entry in [positions], tallest bar highlighted,
 * count printed above each bar. Originally built for the player-profile screen's all-time usage
 * (callers there pass `state.strategy.positions`, since a single strategy's columns is exactly
 * right once a selector resolves which strategy); the tournament statistics screen instead passes
 * the union of positions actually played across the tournament, since defaulting to one strategy's
 * column set would silently hide real data for a team that played under a different strategy
 * (e.g. softball's 4-outfielder Slowpitch vs the usual 3) or mixed strategies across lineups.
 */
@Composable
fun PositionUsageChart(
    positionsData: Map<FieldPosition, Int>,
    positions: List<FieldPosition>,
    teamType: TeamType
) {
    val context = LocalContext.current
    val positionNames = StringResourcesProviderImpl(context).positionShortNames(teamType.id)
    val maxCount = positionsData.values.maxOrNull()?.takeIf { it > 0 } ?: 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // The count label reserves its own space at the top of the chart area so the tallest
        // bar (+ its label) still fits exactly within chartHeight, but every other bar's label
        // floats right above that bar's own top edge instead of all labels lining up flush at
        // the chart's top regardless of height.
        val chartHeight = 88.dp
        val labelReserve = 18.dp
        val barMaxHeight = chartHeight - labelReserve

        positions.forEach { position ->
            val count = positionsData[position] ?: 0
            val isHighlighted = count > 0 && count == maxCount
            val barHeight = barMaxHeight * (count / maxCount.toFloat())
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (count > 0) {
                            Text(
                                text = count.toString(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barHeight)
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

@DugoutPreviews
@Composable
private fun PositionUsageChartPreview() {
    DugoutTheme {
        PositionUsageChart(
            positionsData = mapOf(
                FieldPosition.SHORT_STOP to 15,
                FieldPosition.SECOND_BASE to 6,
                FieldPosition.THIRD_BASE to 3
            ),
            positions = TeamType.BASEBALL.getStrategies()[0].positions.toList(),
            teamType = TeamType.BASEBALL
        )
    }
}

/**
 * Dashed-outline "nothing to show yet" state for [PositionUsageChart], same empty-state language
 * as every other "nothing here yet" surface in this system. [message] defaults to the one about
 * a single player; a screen about the whole team passes its own.
 */
@Composable
fun PositionUsageEmptyState(
    message: String = stringResource(R.string.player_profile_empty_message)
) {
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
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@DugoutPreviews
@Composable
private fun PositionUsageEmptyStatePreview() {
    DugoutTheme {
        PositionUsageEmptyState()
    }
}

/**
 * Photo-or-dashed-placeholder picker with an overflowing add/delete corner badge, shared by any
 * form that lets a user attach a photo (Team form, Player form) - same dashed-empty-state and
 * sibling-offset-badge language as everything else in this system, just a different corner
 * radius and placeholder glyph per caller ([cornerRadius] = size/2 gives Player form's circle,
 * Team form keeps its own rounded-square ratio). The badge has its own tap target
 * ([onRemoveClick]), separate from tapping the photo itself ([onClick], which always (re)opens
 * the picker) - clearing the photo is a real, distinct action, not just a relabeled "pick again".
 */
@Composable
fun AvatarPicker(
    imageUri: Uri?,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    size: Dp,
    cornerRadius: Dp,
    placeholderIcon: Int,
    modifier: Modifier = Modifier
) {
    val bitmap = rememberDugoutAvatarBitmap(imageUri, size)
    // The badge is a sibling of the clipped photo content, not a child of it, and pushed
    // outward via offset - matches the mockup's avatar-picker__badge, which sits at
    // right:-6px;bottom:-6px, overflowing the tile rather than being cropped by its own
    // rounded corners.
    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .let {
                    if (bitmap == null) {
                        it.dashedBorder(MaterialTheme.colorScheme.outline, cornerRadius)
                    } else {
                        it
                    }
                }
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick),
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
                    painter = painterResource(placeholderIcon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(size * 0.33f)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 8.dp)
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(11.dp))
                .clickable(onClick = if (bitmap != null) onRemoveClick else onClick),
            contentAlignment = Alignment.Center
        ) {
            val badgeIcon = if (bitmap != null) {
                R.drawable.ic_delete_white_24dp
            } else {
                R.drawable.ic_add_white_24dp
            }
            Icon(
                painter = painterResource(badgeIcon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@DugoutPreviews
@Composable
private fun AvatarPickerPreview() {
    DugoutTheme {
        AvatarPicker(
            imageUri = null,
            onClick = {},
            onRemoveClick = {},
            size = 84.dp,
            cornerRadius = 16.dp,
            placeholderIcon = R.drawable.ic_person_silhouette_24dp
        )
    }
}

private fun PlayerSide.shortLabelRes(): Int = when (this) {
    PlayerSide.LEFT -> R.string.player_side_abbr_left
    PlayerSide.RIGHT -> R.string.player_side_abbr_right
    PlayerSide.BOTH -> R.string.player_side_abbr_both
}

/** Shared "L"/"R"/"B" abbreviation for a batting/throwing side (Roster rows, Player profile). */
@Composable
fun PlayerSide.shortLabel(): String = stringResource(shortLabelRes())

@DugoutPreviews
@Composable
private fun PlayerSideShortLabelPreview() {
    DugoutTheme {
        Text(text = PlayerSide.LEFT.shortLabel())
    }
}

/**
 * The mockup's own `.appbar` (icon chip + eyebrow/title column + optional trailing action
 * chips), built as ordinary screen content - every already-Compose screen still sits inside the
 * real drawer/system toolbar, which keeps doing its real job (back navigation, edit/delete
 * menu items) untouched. This header duplicates those same real actions for now, so once the
 * drawer/toolbar are eventually retired (a separate, later pass), nothing here needs to change.
 */
@Composable
fun ScreenHeader(
    icon: Int,
    onIconClick: () -> Unit,
    iconContentDescription: String?,
    eyebrow: String?,
    title: String,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        HeaderIconChip(
            icon = icon,
            contentDescription = iconContentDescription,
            onClick = onIconClick
        )
        Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            // One line, shrunk to fit: a long translation would otherwise break mid-word next
            // to the header's action chips on a small screen.
            val titleStyle = MaterialTheme.typography.titleMedium
            BasicText(
                text = title,
                style = titleStyle.copy(color = MaterialTheme.colorScheme.onBackground),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = HEADER_TITLE_MIN_FONT_SIZE,
                    maxFontSize = titleStyle.fontSize
                )
            )
        }
        actions?.invoke(this)
    }
}

@DugoutPreviews
@Composable
private fun ScreenHeaderPreview() {
    DugoutTheme {
        ScreenHeader(
            icon = R.drawable.ic_arrow_left_white_24dp,
            onIconClick = {},
            iconContentDescription = "Back",
            eyebrow = "Settings",
            title = "Team details",
            actions = {
                HeaderActionIconChip(
                    icon = R.drawable.ic_edit_outline_24dp,
                    contentDescription = "Edit",
                    onClick = {}
                )
            }
        )
    }
}

/** The header's own leading icon - a 30dp neutral rounded-square chip (mockup's `appbar__icon`). */
@Composable
fun HeaderIconChip(icon: Int, contentDescription: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@DugoutPreviews
@Composable
private fun HeaderIconChipPreview() {
    DugoutTheme {
        HeaderIconChip(
            icon = R.drawable.ic_arrow_left_white_24dp,
            contentDescription = "Back",
            onClick = {}
        )
    }
}

/**
 * A header's trailing action icon - a larger 34dp accent-tinted chip (mockup's `appbar__action`),
 * visually distinct from the neutral leading [HeaderIconChip].
 */
@Composable
fun HeaderActionIconChip(icon: Int, contentDescription: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(17.dp)
        )
    }
}

@DugoutPreviews
@Composable
private fun HeaderActionIconChipPreview() {
    DugoutTheme {
        HeaderActionIconChip(
            icon = R.drawable.ic_edit_outline_24dp,
            contentDescription = "Edit",
            onClick = {}
        )
    }
}

/**
 * A destructive/discard confirmation bottom sheet (mockup's `.sheet` + `.formbar--danger`),
 * replacing a native `AlertDialog` (`DialogFactory`) with the same two-button shape the mockup
 * uses everywhere a confirm/cancel choice is destructive - "Keep editing"/"Discard", "Cancel"/
 * "Delete team", etc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmSheet(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 22.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = cancelLabel, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        onDismiss()
                        onConfirm()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = confirmLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@DugoutPreviews
@Composable
private fun ConfirmSheetPreview() {
    DugoutTheme {
        ConfirmSheet(
            title = "Discard changes?",
            message = "You have unsaved changes that will be lost.",
            confirmLabel = "Discard",
            cancelLabel = "Keep editing",
            onDismiss = {},
            onConfirm = {}
        )
    }
}

/**
 * The 4 top-level destinations the mockup's own `.bottomnav` component switches between,
 * replacing the retired drawer's tab-switching role. `route` matches the one
 * [com.telen.easylineup.navigation.AppNavHost] registers for each of them.
 */
enum class BottomNavTab(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    val route: Routes
) {
    HOME(R.string.title_last_lineup, R.drawable.ic_dashboard_black_24dp, Routes.Dashboard),
    ROSTER(R.string.title_team, R.drawable.ic_team, Routes.Roster),
    LINEUPS(R.string.title_lineups, R.drawable.ic_list_black_24dp, Routes.Tournaments),
    SETTINGS(R.string.title_settings, R.drawable.ic_settings_black_24dp, Routes.Settings)
}

@Composable
fun DugoutBottomNav(selected: BottomNavTab?, onSelect: (BottomNavTab) -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(top = 10.dp, bottom = 16.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        BottomNavTab.values().forEach { tab ->
            val active = tab == selected
            val tint = if (active) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onSelect(tab) }
                    .padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(tab.iconRes),
                    contentDescription = stringResource(tab.labelRes),
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(tab.labelRes).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = tint
                )
            }
        }
    }
}

@DugoutPreviews
@Composable
private fun DugoutBottomNavPreview() {
    DugoutTheme {
        DugoutBottomNav(selected = BottomNavTab.HOME, onSelect = {})
    }
}

/**
 * Shared form chrome - promoted here once the Edit Lineup sheet needed the exact
 * same name-field/select-field/cancel+save-formbar pieces the Create Lineup
 * screen already built, per this file's own promotion rule: extracted once a
 * second screen needed the same chrome.
 */
@Composable
fun FormLabel(text: String, required: Boolean = false) {
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

@DugoutPreviews
@Composable
private fun FormLabelPreview() {
    DugoutTheme {
        FormLabel(text = "Team name", required = true)
    }
}

@Composable
fun SelectField(
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
        if (label.isNotBlank()) {
            FormLabel(label, required)
            Spacer(modifier = Modifier.height(6.dp))
        }
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

@DugoutPreviews
@Composable
private fun SelectFieldPreview() {
    DugoutTheme {
        SelectField(
            label = "Team",
            value = "Riverside Hawks",
            placeholder = "Select a team",
            subLabel = "Tap to change",
            required = true,
            onClick = {}
        )
    }
}

/**
 * For the narrow third-width button of a form bar: the default 24dp side padding leaves too
 * little room for a translated "Cancel", which then breaks mid-word.
 */
val NARROW_BUTTON_PADDING = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
private val HEADER_TITLE_MIN_FONT_SIZE = 12.sp

@Composable
fun FormBar(
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
                shape = RoundedCornerShape(12.dp),
                contentPadding = NARROW_BUTTON_PADDING
            ) {
                Text(
                    text = cancelLabel,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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

@DugoutPreviews
@Composable
private fun FormBarPreview() {
    DugoutTheme {
        FormBar(
            cancelLabel = "Cancel",
            saveLabel = "Save",
            onCancel = {},
            onSave = {}
        )
    }
}
