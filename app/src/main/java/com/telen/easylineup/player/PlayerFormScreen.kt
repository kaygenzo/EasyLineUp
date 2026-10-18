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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.AvatarPicker
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.utils.StringResourcesProviderImpl

data class PlayerFormState(
    val name: String,
    val nameError: String?,
    val shirtNumberText: String,
    val licenseNumberText: String,
    val imageUri: Uri?,
    val email: String,
    val emailError: String?,
    val phone: String,
    val phoneError: String?,
    val sex: Sex,
    val battingLeft: Boolean,
    val battingRight: Boolean,
    val pitchingLeft: Boolean,
    val pitchingRight: Boolean,
    val selectedPositions: Set<FieldPosition>,
    val availablePositions: List<FieldPosition>,
    val teamType: TeamType
)

@Composable
fun PlayerFormScreen(
    state: PlayerFormState,
    onNameChanged: (String) -> Unit,
    onShirtNumberChanged: (String) -> Unit,
    onLicenseNumberChanged: (String) -> Unit,
    onAvatarClick: () -> Unit,
    onAvatarRemoveClick: () -> Unit,
    onSexSelected: (Sex) -> Unit,
    onBattingLeftToggled: () -> Unit,
    onBattingRightToggled: () -> Unit,
    onPitchingLeftToggled: () -> Unit,
    onPitchingRightToggled: () -> Unit,
    onPositionToggled: (FieldPosition) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            AvatarPicker(
                imageUri = state.imageUri,
                onClick = onAvatarClick,
                onRemoveClick = onAvatarRemoveClick,
                size = 72.dp,
                cornerRadius = 36.dp,
                placeholderIcon = R.drawable.ic_person_silhouette_24dp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SexRow(selected = state.sex, onSexSelected = onSexSelected)

            Spacer(modifier = Modifier.height(18.dp))

            RequiredField(
                label = stringResource(R.string.player_hint_name),
                value = state.name,
                error = state.nameError,
                onValueChanged = onNameChanged
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OptionalField(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.player_hint_shirt_number),
                    value = state.shirtNumberText,
                    onValueChanged = onShirtNumberChanged,
                    keyboardType = KeyboardType.Number
                )
                OptionalField(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.player_hint_license_number_short),
                    value = state.licenseNumberText,
                    onValueChanged = onLicenseNumberChanged,
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            SideToggleRow(
                label = stringResource(R.string.create_player_pitching_side),
                leftSelected = state.pitchingLeft,
                rightSelected = state.pitchingRight,
                onLeftToggled = onPitchingLeftToggled,
                onRightToggled = onPitchingRightToggled
            )

            Spacer(modifier = Modifier.height(10.dp))

            SideToggleRow(
                label = stringResource(R.string.create_player_batting_side),
                leftSelected = state.battingLeft,
                rightSelected = state.battingRight,
                onLeftToggled = onBattingLeftToggled,
                onRightToggled = onBattingRightToggled
            )

            Spacer(modifier = Modifier.height(18.dp))

            FavoritePositions(
                availablePositions = state.availablePositions,
                selectedPositions = state.selectedPositions,
                teamType = state.teamType,
                onPositionToggled = onPositionToggled
            )

            Spacer(modifier = Modifier.height(14.dp))

            OptionalField(
                label = stringResource(R.string.player_hint_email),
                value = state.email,
                error = state.emailError,
                onValueChanged = onEmailChanged,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(14.dp))

            OptionalField(
                label = stringResource(R.string.player_hint_phone),
                value = state.phone,
                error = state.phoneError,
                onValueChanged = onPhoneChanged,
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(18.dp))
        }

        FormBar(onSaveClick = onSaveClick)
    }
}

@Composable
private fun SexRow(selected: Sex, onSexSelected: (Sex) -> Unit) {
    Column {
        Text(
            text = stringResource(R.string.create_player_sex).uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormPill(
                text = stringResource(R.string.setting_men_style_description),
                selected = selected == Sex.MALE,
                onClick = {
                    onSexSelected(if (selected == Sex.MALE) Sex.UNKNOWN else Sex.MALE)
                }
            )
            FormPill(
                text = stringResource(R.string.setting_women_style_description),
                selected = selected == Sex.FEMALE,
                onClick = {
                    onSexSelected(if (selected == Sex.FEMALE) Sex.UNKNOWN else Sex.FEMALE)
                }
            )
        }
    }
}

@Composable
private fun SideToggleRow(
    label: String,
    leftSelected: Boolean,
    rightSelected: Boolean,
    onLeftToggled: () -> Unit,
    onRightToggled: () -> Unit
) {
    Column {
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormPill(
                text = stringResource(R.string.generic_left),
                selected = leftSelected,
                onClick = onLeftToggled
            )
            FormPill(
                text = stringResource(R.string.generic_right),
                selected = rightSelected,
                onClick = onRightToggled
            )
        }
    }
}

@Composable
private fun FormPill(text: String, selected: Boolean, onClick: () -> Unit) {
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = contentColor, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun RequiredField(
    label: String,
    value: String,
    error: String?,
    onValueChanged: (String) -> Unit
) {
    Column {
        Row {
            Text(
                text = label.uppercase(),
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
            value = value,
            onValueChange = onValueChanged,
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
private fun OptionalField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    error: String? = null,
    onValueChanged: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = true,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            placeholder = { Text(stringResource(R.string.generic_optional)) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
private fun FavoritePositions(
    availablePositions: List<FieldPosition>,
    selectedPositions: Set<FieldPosition>,
    teamType: TeamType,
    onPositionToggled: (FieldPosition) -> Unit
) {
    val context = LocalContext.current
    val names = StringResourcesProviderImpl(context).positionShortNames(teamType.id)
    Column {
        Text(
            text = stringResource(R.string.create_player_favorite_positions).uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            availablePositions.forEach { position ->
                val selected = position in selectedPositions
                FormPill(
                    text = names.getOrNull(position.id) ?: "",
                    selected = selected,
                    onClick = { onPositionToggled(position) }
                )
            }
        }
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
private fun PlayerFormScreenCreatePreview() {
    DugoutTheme {
        PlayerFormScreen(
            state = PlayerFormState(
                name = "",
                nameError = null,
                shirtNumberText = "",
                licenseNumberText = "",
                imageUri = null,
                email = "",
                emailError = null,
                phone = "",
                phoneError = null,
                sex = Sex.UNKNOWN,
                battingLeft = false,
                battingRight = false,
                pitchingLeft = false,
                pitchingRight = false,
                selectedPositions = emptySet(),
                availablePositions = FieldPosition.values().filter { it.isDefensePlayer() }
                    .distinctBy { it.mask },
                teamType = TeamType.BASEBALL
            ),
            onNameChanged = {},
            onShirtNumberChanged = {},
            onLicenseNumberChanged = {},
            onAvatarClick = {},
            onAvatarRemoveClick = {},
            onSexSelected = {},
            onBattingLeftToggled = {},
            onBattingRightToggled = {},
            onPitchingLeftToggled = {},
            onPitchingRightToggled = {},
            onPositionToggled = {},
            onEmailChanged = {},
            onPhoneChanged = {},
            onSaveClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun PlayerFormScreenEditPreview() {
    DugoutTheme {
        PlayerFormScreen(
            state = PlayerFormState(
                name = "Jonas Alvarez",
                nameError = null,
                shirtNumberText = "7",
                licenseNumberText = "",
                imageUri = null,
                email = "jonas.a@mail.com",
                emailError = null,
                phone = "",
                phoneError = null,
                sex = Sex.MALE,
                battingLeft = true,
                battingRight = true,
                pitchingLeft = false,
                pitchingRight = true,
                selectedPositions = setOf(
                    FieldPosition.SHORT_STOP,
                    FieldPosition.SECOND_BASE,
                    FieldPosition.THIRD_BASE
                ),
                availablePositions = FieldPosition.values().filter { it.isDefensePlayer() }
                    .distinctBy { it.mask },
                teamType = TeamType.BASEBALL
            ),
            onNameChanged = {},
            onShirtNumberChanged = {},
            onLicenseNumberChanged = {},
            onAvatarClick = {},
            onAvatarRemoveClick = {},
            onSexSelected = {},
            onBattingLeftToggled = {},
            onBattingRightToggled = {},
            onPitchingLeftToggled = {},
            onPitchingRightToggled = {},
            onPositionToggled = {},
            onEmailChanged = {},
            onPhoneChanged = {},
            onSaveClick = {}
        )
    }
}
