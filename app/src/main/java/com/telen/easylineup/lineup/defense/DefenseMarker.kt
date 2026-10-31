/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.defense

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.rememberDugoutAvatarBitmap
import com.telen.easylineup.utils.toLetters
import com.telen.easylineup.views.StateDefense

/**
 * Circular field-position/player marker replicating the legacy
 * `MultipleStateDefenseIconButton`'s exact states - the one spot in the whole design system
 * where "empty" is a solid filled circle, not the rest of the app's dashed-border convention.
 */
@Composable
fun DefenseMarker(
    state: StateDefense,
    imageUri: Uri?,
    fallbackName: String,
    size: Dp,
    borderColor: Color,
    label: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enableClick: Boolean = true
) {
    val isFilledCircle = state == StateDefense.ADD_PLAYER ||
        state == StateDefense.DP_DH ||
        state == StateDefense.EMPTY ||
        state == StateDefense.LOADING
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (isFilledCircle) {
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                } else {
                    Modifier
                }
            )
            .then(
                if (enableClick) {
                    Modifier.clickable(
                        enabled = state != StateDefense.EMPTY && state != StateDefense.NONE,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            StateDefense.LOADING -> CircularProgressIndicator(modifier = Modifier.size(size * 0.5f))

            StateDefense.ADD_PLAYER -> Icon(
                painter = painterResource(R.drawable.ic_add_white_24dp),
                contentDescription =
                stringResource(R.string.defense_empty_position_content_description),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(size * 0.5f)
            )

            StateDefense.DP_DH -> Text(
                text = label.orEmpty(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )

            StateDefense.PLAYER -> PlayerMarkerContent(
                imageUri = imageUri,
                fallbackName = fallbackName,
                size = size,
                borderColor = borderColor
            )

            StateDefense.NONE, StateDefense.EMPTY -> Unit
        }
    }
}

@Composable
private fun PlayerMarkerContent(
    imageUri: Uri?,
    fallbackName: String,
    size: Dp,
    borderColor: Color
) {
    val bitmap = rememberDugoutAvatarBitmap(imageUri, size)
    val borderWidth = if (borderColor == Color.Red) 3.dp else 2.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                painter = BitmapPainter(bitmap),
                contentDescription = fallbackName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            Text(
                text = fallbackName.toLetters(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.33f).sp
            )
        }
    }
}

/** Circular delete target, matching the legacy trash FAB's 1x/2x scale-on-drag-over animation. */
@Composable
fun DefenseTrashButton(size: Dp, isDragTarget: Boolean, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (isDragTarget) 2f else 1f,
        label = "trashScale"
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_delete_24dp),
            contentDescription = stringResource(R.string.defense_trash_content_description),
            tint = Color.White,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

/**
 * Small filled dot overlaying a player marker's top-right corner, replicating the legacy
 * canvas-drawn sex indicator (center exactly at the marker's corner point).
 */
@Composable
fun SexIndicatorDot(color: Color, dotSize: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(dotSize)
            .clip(CircleShape)
            .background(color)
    )
}

@DugoutPreviews
@Composable
private fun DefenseMarkerPreview() {
    DugoutTheme {
        DefenseMarker(
            state = StateDefense.PLAYER,
            imageUri = null,
            fallbackName = "Mia Chen",
            size = 56.dp,
            borderColor = Color.Black,
            label = null,
            onClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun PlayerMarkerContentPreview() {
    DugoutTheme {
        PlayerMarkerContent(
            imageUri = null,
            fallbackName = "Mia Chen",
            size = 56.dp,
            borderColor = Color.Black
        )
    }
}

@DugoutPreviews
@Composable
private fun DefenseTrashButtonPreview() {
    DugoutTheme {
        DefenseTrashButton(size = 56.dp, isDragTarget = true)
    }
}

@DugoutPreviews
@Composable
private fun SexIndicatorDotPreview() {
    DugoutTheme {
        SexIndicatorDot(color = Color.Red, dotSize = 14.dp)
    }
}
