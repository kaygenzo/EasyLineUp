package com.telen.easylineup.lineup.components

//import android.annotation.SuppressLint
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxWithConstraints
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.TextUnit
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.min
//import coil.compose.SubcomposeAsyncImage
//
//private const val TEXT_SIZE_RATIO = 0.5f
//
//@SuppressLint("UnusedBoxWithConstraintsScope")
//@Composable
//fun PlayerRoundedItem(
//    modifier: Modifier = Modifier,
//    playerName: String,
//    imageUrl: String? = null,
//    borderColor: Color
//) {
//    val initials = playerName.split(" ")
//        .filter { it.isNotBlank() }
//        .take(2)
//        .map { it.first().uppercase() }
//        .joinToString("")
//
//    BoxWithConstraints(
//        modifier = modifier
//            .clip(CircleShape)
//            .background(Color.White)
//            .border(2.dp, borderColor, CircleShape),
//        contentAlignment = Alignment.Center
//    ) {
//        val fontSize = with(LocalDensity.current) {
//            (min(maxWidth, maxHeight) * TEXT_SIZE_RATIO).toSp()
//        }
//
//        if (!imageUrl.isNullOrBlank()) {
//            SubcomposeAsyncImage(
//                model = imageUrl,
//                contentDescription = playerName,
//                modifier = Modifier.fillMaxSize(),
//                contentScale = ContentScale.Crop,
//                error = {
//                    InitialsView(initials = initials, fontSize = fontSize)
//                },
//                loading = {
//                    InitialsView(initials = initials, fontSize = fontSize)
//                }
//            )
//        } else {
//            InitialsView(initials = initials, fontSize = fontSize)
//        }
//    }
//}
//
//@Composable
//private fun InitialsView(initials: String, fontSize: TextUnit) {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = initials,
//            style = MaterialTheme.typography.bodyMedium.copy(
//                fontWeight = FontWeight.Bold,
//                fontSize = fontSize
//            ),
//            color = Color.Black,
//            textAlign = TextAlign.Center
//        )
//    }
//}
//
//@Preview
//@Composable
//private fun PlayerRoundedItemPreview() {
//    PlayerRoundedItem(
//        playerName = "John Doe",
//        borderColor = Color.Blue,
//        modifier = Modifier.size(64.dp)
//    )
//}
//
//@Preview
//@Composable
//private fun PlayerRoundedItemWithImagePreview() {
//    PlayerRoundedItem(
//        playerName = "John Doe",
//        borderColor = Color.Red,
//        imageUrl = "https://example.com/image.jpg",
//        modifier = Modifier.size(64.dp)
//    )
//}
