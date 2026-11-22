package com.jjcr11.rss.di

import com.jjcr11.rss.domain.usecase.AddSourceUseCase
import com.jjcr11.rss.domain.usecase.GetFeedsUseCase
import com.jjcr11.rss.domain.usecase.GetSourcesUseCase
import com.jjcr11.rss.domain.usecase.ListenToRealtimeSourcesUseCase
import com.jjcr11.rss.domain.usecase.ObserveAuthStateUseCase
import com.jjcr11.rss.domain.usecase.SignUpUseCase
import com.jjcr11.rss.domain.usecase.SyncSourcesUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory {
        AddSourceUseCase(
            repository = get(),
        )
    }
    factory {
        GetSourcesUseCase(
            repository = get(),
        )
    }
    factory {
        GetFeedsUseCase(
            repository = get(),
        )
    }
    factory {
        SyncSourcesUseCase(
            repository = get(),
        )
    }
    factory {
        ListenToRealtimeSourcesUseCase(
            repository = get(),
        )
    }
    factory {
        SignUpUseCase(
            repository = get(),
        )
    }
    factory {
        ObserveAuthStateUseCase(
            repository = get(),
        )
    }
}
