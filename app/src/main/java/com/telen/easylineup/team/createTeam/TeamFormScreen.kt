/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telen.easylineup.R
import com.telen.easylineup.compose.AvatarPicker
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.ScreenHeader
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.utils.titleRes

// Matches the mockup's own type-card__icon glyphs exactly (it uses plain emoji, not SVG, for
// this one spot) - flat and lightweight, unlike the photo-textured pitcher() art REDESIGN.md's
// own "no photo-textured assets" rule is about getting rid of.
private fun TeamType.emoji(): String = when (this) {
    TeamType.SOFTBALL -> "🥎"
    else -> "⚾"
}

data class TeamFormState(
    val isEditing: Boolean,
    val teamName: String,
    val teamImageUri: Uri?,
    val teamType: TeamType,
    val nameError: String?
)

@Composable
fun TeamFormScreen(
    state: TeamFormState,
    onCloseClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onAvatarRemoveClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onTypeSelected: (TeamType) -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        FormAppBar(isEditing = state.isEditing, onCloseClick = onCloseClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            AvatarPicker(
                imageUri = state.teamImageUri,
                onClick = onAvatarClick,
                onRemoveClick = onAvatarRemoveClick,
                size = 72.dp,
                cornerRadius = 20.dp,
                placeholderIcon = R.drawable.ic_image_placeholder_24dp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            NameField(
                name = state.teamName,
                error = state.nameError,
                onNameChanged = onNameChanged
            )

            Spacer(modifier = Modifier.height(18.dp))

            TypeField(selectedType = state.teamType, onTypeSelected = onTypeSelected)

            Spacer(modifier = Modifier.height(18.dp))
        }

        FormBar(onSaveClick = onSaveClick)
    }
}

@Composable
private fun FormAppBar(isEditing: Boolean, onCloseClick: () -> Unit) {
    ScreenHeader(
        icon = R.drawable.ic_close_24dp,
        onIconClick = onCloseClick,
        iconContentDescription = stringResource(R.string.team_form_close_content_description),
        eyebrow = stringResource(R.string.team_form_eyebrow),
        title = stringResource(
            if (isEditing) R.string.team_form_title_edit else R.string.team_form_title_create
        ),
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 14.dp)
    )
}

@Composable
private fun NameField(name: String, error: String?, onNameChanged: (String) -> Unit) {
    Column {
        Row {
            Text(
                text = stringResource(R.string.team_form_name_label).uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "*",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            singleLine = true,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
private fun TypeField(selectedType: TeamType, onTypeSelected: (TeamType) -> Unit) {
    Column {
        Text(
            text = stringResource(R.string.team_form_type_label).uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(TeamType.BASEBALL, TeamType.SOFTBALL, TeamType.BASEBALL_5).forEach { type ->
                TypeCard(
                    type = type,
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TypeCard(
    type: TeamType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = type.emoji(),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = stringResource(type.titleRes()).uppercase(),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun FormBar(onSaveClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 18.dp)
        ) {
            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = stringResource(R.string.save_button), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@DugoutPreviews
@Composable
private fun TeamFormScreenCreatePreview() {
    DugoutTheme {
        TeamFormScreen(
            state = TeamFormState(
                isEditing = false,
                teamName = "",
                teamImageUri = null,
                teamType = TeamType.BASEBALL,
                nameError = null
            ),
            onCloseClick = {},
            onAvatarClick = {},
            onAvatarRemoveClick = {},
            onNameChanged = {},
            onTypeSelected = {},
            onSaveClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun TeamFormScreenEditPreview() {
    DugoutTheme {
        TeamFormScreen(
            state = TeamFormState(
                isEditing = true,
                teamName = "Riverside Hawks",
                teamImageUri = null,
                teamType = TeamType.SOFTBALL,
                nameError = null
            ),
            onCloseClick = {},
            onAvatarClick = {},
            onAvatarRemoveClick = {},
            onNameChanged = {},
            onTypeSelected = {},
            onSaveClick = {}
        )
    }
}
