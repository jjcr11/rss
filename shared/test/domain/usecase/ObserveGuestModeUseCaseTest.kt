package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGuestModeUseCaseTest {

    private class FakePreferencesRepository(
        override val isGuestMode: Flow<Boolean>,
    ) : PreferencesRepository {
        override suspend fun setGuestMode(isGuestMode: Boolean) {
            throw NotImplementedError()
        }
    }

    @Test
    fun invoke_returnsIsGuestModeFlowFromRepository() = runTest {
        val repository = FakePreferencesRepository(
            isGuestMode = flowOf(value = true),
        )
        val useCase = ObserveGuestModeUseCase(
            repository = repository,
        )

        val result = useCase().first()

        assertEquals(
            expected = true,
            actual = result,
        )
    }
}
