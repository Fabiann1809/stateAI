package com.stateai.ui.feedback

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.SegmentId
import com.stateai.ui.components.FeedbackFaces
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/** One-tap "how did you feel?" after a segment ends. "Omitir" or swiping back skips it. */
@Composable
fun FeedbackRoute(segmentId: SegmentId, onDone: () -> Unit) {
    val container = appContainer()
    val viewModel: FeedbackViewModel = viewModel(
        factory = viewModelFactory { initializer { FeedbackViewModel(segmentId, container.recordFeedback) } },
    )
    FeedbackScreen(onAnswer = { viewModel.answer(it, onDone) }, onSkip = onDone)
}

@Composable
fun FeedbackScreen(onAnswer: (Feedback) -> Unit, onSkip: () -> Unit) {
    ScreenScaffold(timeText = {}) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceL),
            ) {
                Text(
                    stringResource(R.string.feedback_title),
                    fontSize = StateAiDimens.Title,
                    fontWeight = FontWeight.Medium,
                    color = StateAiColors.Text2,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Feedback.entries.forEach { feedback -> FaceOption(feedback, onClick = { onAnswer(feedback) }) }
                }
                Text(
                    text = stringResource(R.string.feedback_skip),
                    fontSize = StateAiDimens.Body,
                    color = StateAiColors.Text3,
                    modifier = Modifier
                        .heightIn(min = StateAiDimens.TouchTarget)
                        .clickable(onClick = onSkip)
                        .padding(horizontal = StateAiDimens.SpaceL, vertical = StateAiDimens.SpaceM),
                )
            }
        }
    }
}

@Composable
private fun FaceOption(feedback: Feedback, onClick: () -> Unit) {
    val label = stringResource(feedback.labelRes())
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(FACE_BUTTON),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = StateAiColors.Surface2,
                contentColor = StateAiColors.Text1,
            ),
        ) {
            Icon(FeedbackFaces.of(feedback), contentDescription = label, modifier = Modifier.size(FACE_ICON))
        }
        Text(label, fontSize = StateAiDimens.Body, fontWeight = FontWeight.Medium)
    }
}

@StringRes
private fun Feedback.labelRes(): Int = when (this) {
    Feedback.GOOD -> R.string.feedback_good
    Feedback.OKAY -> R.string.feedback_okay
    Feedback.BAD -> R.string.feedback_bad
}

private val FACE_BUTTON = 52.dp
private val FACE_ICON = 30.dp
