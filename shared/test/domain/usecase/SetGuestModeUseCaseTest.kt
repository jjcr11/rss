package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SetGuestModeUseCaseTest {

    private class FakePreferencesRepository : PreferencesRepository {
        var updatedGuestMode: Boolean? = null

        override val isGuestMode: Flow<Boolean> = emptyFlow()

        override suspend fun setGuestMode(isGuestMode: Boolean) {
            updatedGuestMode = isGuestMode
        }
    }

    @Test
    fun invoke_delegatesToRepositoryWithGivenValue() = runTest {
        val repository = FakePreferencesRepository()
        val useCase = SetGuestModeUseCase(
            repository = repository,
        )

        useCase(isGuestMode = true)

        assertEquals(
            expected = true,
            actual = repository.updatedGuestMode,
        )
    }
}
