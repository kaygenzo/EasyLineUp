/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.login

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme

private data class ValueProp(@DrawableRes val iconRes: Int, @StringRes val textRes: Int)

private val valueProps = listOf(
    ValueProp(R.drawable.ic_welcome_lineup, R.string.welcome_value_lineups),
    ValueProp(R.drawable.ic_welcome_defense, R.string.welcome_value_defense),
    ValueProp(R.drawable.ic_welcome_message, R.string.welcome_value_message)
)

@Composable
fun WelcomeScreen(
    onCreateTeam: () -> Unit,
    onImportTeam: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .displayCutoutPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 22.dp, end = 22.dp, top = 30.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 38.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Text(
            text = stringResource(R.string.welcome_headline),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = stringResource(R.string.welcome_body),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .widthIn(max = 240.dp)
                .padding(bottom = 26.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            valueProps.forEach { prop ->
                ValuePropRow(prop)
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 32.dp, bottom = 26.dp)
        ) {
            Button(
                onClick = onCreateTeam,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.login_create),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = onImportTeam,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = stringResource(R.string.login_import),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ValuePropRow(prop: ValueProp) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    RoundedCornerShape(11.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(prop.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp)
            )
        }
        Text(
            text = stringResource(prop.textRes),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@DugoutPreviews
@Composable
private fun ValuePropRowPreview() {
    DugoutTheme {
        ValuePropRow(
            ValueProp(R.drawable.ic_welcome_lineup, R.string.welcome_value_lineups)
        )
    }
}

@DugoutPreviews
@Composable
private fun WelcomeScreenPreview() {
    DugoutTheme {
        WelcomeScreen(onCreateTeam = {}, onImportTeam = {})
    }
}
