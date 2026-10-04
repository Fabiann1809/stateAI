package com.stateai.ui.session

import com.stateai.domain.activity.Activity
import com.stateai.domain.session.SessionProgress

/** What the session screen shows; null fields mean the session is still starting. */
data class SessionUiState(val activity: Activity? = null, val progress: SessionProgress? = null)
