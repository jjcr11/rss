package com.jjcr11.rss.di

import com.jjcr11.rss.domain.manager.SyncManager
import com.jjcr11.rss.domain.usecase.AddSourceUseCase
import com.jjcr11.rss.domain.usecase.AuthenticateWithEmailUseCase
import com.jjcr11.rss.domain.usecase.DeleteSourceUseCase
import com.jjcr11.rss.domain.usecase.FetchFeedsFromSourcesUseCase
import com.jjcr11.rss.domain.usecase.GetFeedsUseCase
import com.jjcr11.rss.domain.usecase.GetSourcesUseCase
import com.jjcr11.rss.domain.usecase.ListenToRealtimeSourcesUseCase
import com.jjcr11.rss.domain.usecase.LogoutUseCase
import com.jjcr11.rss.domain.usecase.ObserveAuthStateUseCase
import com.jjcr11.rss.domain.usecase.ObserveGuestModeUseCase
import com.jjcr11.rss.domain.usecase.SetFeedAsReadUseCase
import com.jjcr11.rss.domain.usecase.SetGuestModeUseCase
import com.jjcr11.rss.domain.usecase.SignInUseCase
import com.jjcr11.rss.domain.usecase.SignUpUseCase
import com.jjcr11.rss.domain.usecase.SyncFeedsUseCase
import com.jjcr11.rss.domain.usecase.SyncSourcesUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory {
        AddSourceUseCase(
            repository = get(),
        )
    }
    factory {
        DeleteSourceUseCase(
            repository = get(),
        )
    }
    factory {
        FetchFeedsFromSourcesUseCase(
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
        SyncFeedsUseCase(
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
        SignInUseCase(
            repository = get(),
        )
    }
    factory {
        AuthenticateWithEmailUseCase(
            repository = get(),
        )
    }
    factory {
        ObserveAuthStateUseCase(
            repository = get(),
        )
    }
    factory {
        SetFeedAsReadUseCase(
            repository = get(),
        )
    }
    factory {
        ObserveGuestModeUseCase(
            repository = get(),
        )
    }
    factory {
        SetGuestModeUseCase(
            repository = get(),
        )
    }
    factory {
        LogoutUseCase(
            repository = get(),
        )
    }
    single {
        SyncManager(
            syncSourcesUseCase = get(),
            syncFeedsUseCase = get(),
            fetchFeedsFromSourcesUseCase = get(),
        )
    }
}
