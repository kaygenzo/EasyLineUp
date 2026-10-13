/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

/**
 * Shared flat card container for the Dugout Card design system - rounded corners, no elevation,
 * surface color. Used by every screen that groups rows/content into a card (Settings, Team
 * Details, ...). Extracted once a second screen needed the same chrome, instead of re-copying
 * a private composable.
 */
@Composable
fun DugoutCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column { content() }
    }
}

/**
 * Shared avatar for anything identified by a name and an optional photo (teams today, players
 * tomorrow): shows the Picasso-loaded photo when [imageUri] is set, otherwise a rounded tile with
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

private fun String.avatarInitials(): String =
    trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

@Composable
private fun rememberDugoutAvatarBitmap(uri: Uri?, size: Dp): ImageBitmap? {
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    val sizePx = with(LocalDensity.current) { size.roundToPx() }

    DisposableEffect(uri) {
        bitmap = null
        if (uri == null) {
            return@DisposableEffect onDispose {}
        }
        val target = object : Target {
            override fun onBitmapLoaded(
                loaded: android.graphics.Bitmap,
                from: Picasso.LoadedFrom
            ) {
                bitmap = loaded.asImageBitmap()
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                bitmap = null
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }
        Picasso.get()
            .load(uri)
            .resize(sizePx, sizePx)
            .centerCrop()
            .into(target)

        onDispose { Picasso.get().cancelRequest(target) }
    }
    return bitmap
}
