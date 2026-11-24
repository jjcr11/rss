package com.jjcr11.rss.di

import com.jjcr11.rss.presentation.addsourcescreen.AddSourceViewModel
import com.jjcr11.rss.presentation.authscreen.AuthViewModel
import com.jjcr11.rss.presentation.feeddetail.FeedDetailViewModel
import com.jjcr11.rss.presentation.feedsscreen.FeedsViewModel
import com.jjcr11.rss.presentation.navigation.NavigationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        AddSourceViewModel(
            addSourceUseCase = get(),
            getSourcesUseCase = get(),
            listenToRealtimeSourcesUseCase = get(),
            deleteSourceUseCase = get(),
        )
    }
    viewModel {
        FeedsViewModel(
            getFeedsUseCase = get(),
            getSourcesUseCase = get(),
        )
    }
    viewModel {
        FeedDetailViewModel(
            getFeedsUseCase = get(),
            getSourcesUseCase = get(),
            setFeedAsReadUseCase = get(),
        )
    }
    viewModel {
        AuthViewModel(
            authenticateWithEmailUseCase = get(),
            setGuestModeUseCase = get(),
        )
    }
    viewModel {
        NavigationViewModel(
            observeAuthStateUseCase = get(),
            syncManager = get(),
            observeGuestModeUseCase = get(),
        )
    }
}
