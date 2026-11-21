package com.jjcr11.rss.di

import com.jjcr11.rss.domain.usecase.AddSourceUseCase
import com.jjcr11.rss.domain.usecase.GetFeedsUseCase
import com.jjcr11.rss.domain.usecase.GetSourcesUseCase
import com.jjcr11.rss.domain.usecase.InsertSourceUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory {
        InsertSourceUseCase(
            repository = get(),
        )
    }
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
}
