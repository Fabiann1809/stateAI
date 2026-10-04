package com.stateai.data.memory

import com.stateai.domain.mascot.MascotName
import com.stateai.domain.mascot.MascotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Mascot name kept in memory, for tests and previews. */
class InMemoryMascotRepository(initial: String? = null) : MascotRepository {
    private val name = MutableStateFlow(initial)

    override fun observeName(): Flow<String?> = name

    override suspend fun saveName(name: String) {
        this.name.value = MascotName.clean(name)
    }
}
