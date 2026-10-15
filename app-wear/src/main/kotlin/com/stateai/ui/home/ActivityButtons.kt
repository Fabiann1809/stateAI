package com.stateai.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.activity.Activity
import com.stateai.ui.common.icon
import com.stateai.ui.common.labelRes
import com.stateai.ui.common.title
import com.stateai.ui.components.StateAiIcons
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/** One activity of the list; the suggested one stands out with the accent border and its tag. */
@Composable
fun ActivityCard(activity: Activity, suggested: Boolean, onClick: () -> Unit) {
    val categoryLabel = stringResource(activity.category.labelRes())
    val border = if (suggested) StateAiColors.Accent else StateAiColors.Outline
    ListCard(
        onClick = onClick,
        container = if (suggested) StateAiColors.AccentSoft else StateAiColors.Surface2,
        border = BorderStroke(if (suggested) SUGGESTED_BORDER else 1.dp, border),
    ) {
        Icon(
            activity.category.icon(),
            contentDescription = null,
            tint = if (suggested) StateAiColors.Accent else StateAiColors.Text2,
            modifier = Modifier.size(ICON_SIZE),
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            if (suggested) {
                Text(
                    text = stringResource(R.string.picker_suggested_tag),
                    fontSize = StateAiDimens.Label,
                    fontWeight = FontWeight.Medium,
                    color = StateAiColors.Accent,
                )
            }
            Text(
                text = activity.title(),
                fontSize = if (suggested) TITLE_SIZE_SUGGESTED else TITLE_SIZE,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            if (!suggested && activity.name != null) {
                Text(categoryLabel, fontSize = StateAiDimens.Label, color = StateAiColors.Text3, maxLines = 1)
            }
        }
    }
}

/** "Nueva", disabled with a short message once there are 10 active activities. */
@Composable
fun NewActivityButton(enabled: Boolean, onClick: () -> Unit) {
    ListCard(
        onClick = onClick,
        enabled = enabled,
        container = StateAiColors.Surface2,
        border = BorderStroke(1.dp, StateAiColors.Outline),
    ) {
        Icon(
            StateAiIcons.Plus,
            contentDescription = null,
            tint = if (enabled) StateAiColors.Accent else StateAiColors.Muted,
            modifier = Modifier.size(ICON_SIZE),
        )
        Column {
            Text(
                text = stringResource(R.string.picker_new),
                fontWeight = FontWeight.SemiBold,
                fontSize = TITLE_SIZE,
                color = if (enabled) StateAiColors.Text1 else StateAiColors.Text3,
            )
            if (!enabled) {
                Text(
                    stringResource(R.string.picker_limit_reached),
                    fontSize = StateAiDimens.Label,
                    color = StateAiColors.Text3,
                )
            }
        }
    }
}

/** A rounded translucent card of the lists: an icon on the left and one or two lines of text. */
@Composable
fun ListCard(
    onClick: () -> Unit,
    container: Color,
    border: BorderStroke,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(CARD_RADIUS)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CARD_MIN_HEIGHT)
            .clip(shape)
            .background(container)
            .border(border, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = CARD_PADDING, vertical = StateAiDimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

private val SUGGESTED_BORDER = 1.5.dp
private val ICON_SIZE = 18.dp
private val CARD_RADIUS = 24.dp
private val CARD_MIN_HEIGHT = 48.dp
private val CARD_PADDING = 14.dp
private val TITLE_SIZE = 14.sp
private val TITLE_SIZE_SUGGESTED = 17.sp
