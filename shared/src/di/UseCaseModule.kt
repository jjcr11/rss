package com.jjcr11.rss.di

import com.jjcr11.rss.domain.usecase.InsertSourceUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory {
        InsertSourceUseCase(
            repository = get(),
        )
    }
}
