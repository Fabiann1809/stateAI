package com.stateai.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.activity.Activity
import com.stateai.ui.common.labelRes
import com.stateai.ui.common.title
import com.stateai.ui.components.StateAiIcons
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/** One activity of the list; the suggested one stands out with the accent border and its tag. */
@Composable
fun ActivityCard(activity: Activity, suggested: Boolean, onClick: () -> Unit) {
    val categoryLabel = stringResource(activity.category.labelRes())
    val container = if (suggested) StateAiColors.Accent.copy(alpha = SUGGESTED_FILL) else StateAiColors.Surface2
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = StateAiColors.Text1),
        border = if (suggested) BorderStroke(1.dp, StateAiColors.Accent.copy(alpha = SUGGESTED_BORDER)) else null,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            if (suggested) {
                Text(
                    text = stringResource(R.string.picker_suggested_tag),
                    fontSize = StateAiDimens.Label,
                    color = StateAiColors.Accent,
                )
            }
            Text(
                text = activity.title(),
                fontSize = if (suggested) TITLE_SIZE_SUGGESTED else TITLE_SIZE,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            if (!suggested && activity.name != null) {
                Text(categoryLabel, fontSize = StateAiDimens.Label, color = StateAiColors.Text2, maxLines = 1)
            }
        }
    }
}

/** "Nueva", disabled with a short message once there are 10 active activities. */
@Composable
fun NewActivityButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        icon = { Icon(StateAiIcons.Plus, contentDescription = null) },
        label = { Text(stringResource(R.string.picker_new), fontWeight = FontWeight.Medium) },
        secondaryLabel = if (enabled) null else ({ Text(stringResource(R.string.picker_limit_reached)) }),
    )
}

private const val SUGGESTED_FILL = 0.14f
private const val SUGGESTED_BORDER = 0.55f
private val TITLE_SIZE = 14.sp
private val TITLE_SIZE_SUGGESTED = 17.sp
