package com.stateai.ui.feedback

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.SegmentId

/** One-tap "how did you feel?" after a segment ends. Swiping back skips it. */
@Composable
fun FeedbackRoute(segmentId: SegmentId, onDone: () -> Unit) {
    val container = appContainer()
    val viewModel: FeedbackViewModel = viewModel(
        factory = viewModelFactory { initializer { FeedbackViewModel(segmentId, container.segmentRepository) } },
    )
    FeedbackScreen(onAnswer = { viewModel.answer(it, onDone) })
}

@Composable
fun FeedbackScreen(onAnswer: (Feedback) -> Unit) {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.feedback_title)) } }
            items(Feedback.entries) { feedback ->
                Button(
                    onClick = { onAnswer(feedback) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(feedback.labelRes())) },
                )
            }
        }
    }
}

@StringRes
private fun Feedback.labelRes(): Int = when (this) {
    Feedback.GOOD -> R.string.feedback_good
    Feedback.OKAY -> R.string.feedback_okay
    Feedback.BAD -> R.string.feedback_bad
}
