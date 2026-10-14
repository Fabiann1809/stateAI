package com.stateai.domain.testing

import com.stateai.domain.pause.PauseResponseRepository
import com.stateai.domain.pause.PauseResponses
import com.stateai.domain.session.SuggestionReason

class FakePauseResponseRepository(initial: Map<SuggestionReason, PauseResponses> = emptyMap()) :
    PauseResponseRepository {
    val stored = initial.toMutableMap()

    override suspend fun load(): Map<SuggestionReason, PauseResponses> = stored.toMap()

    override suspend fun save(reason: SuggestionReason, responses: PauseResponses) {
        stored[reason] = responses
    }
}
