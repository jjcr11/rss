package com.jjcr11.rss.di

import com.jjcr11.rss.presentation.addsourcescreen.AddSourceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        AddSourceViewModel(
            addSourceUseCase = get(),
        )
    }
}
