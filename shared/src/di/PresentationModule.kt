package com.jjcr11.rss.di

import com.jjcr11.rss.presentation.addsourcescreen.AddSourceViewModel
import com.jjcr11.rss.presentation.feedsscreen.FeedsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        AddSourceViewModel(
            addSourceUseCase = get(),
            getSourcesUseCase = get(),
        )
    }
    viewModel {
        FeedsViewModel(
            getFeedsUseCase = get(),
            getSourcesUseCase = get(),
            syncSourcesUseCase = get(),
        )
    }
}
