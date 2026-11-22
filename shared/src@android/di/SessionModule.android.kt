package com.jjcr11.rss.di

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.SettingsSessionManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformSessionModule: Module = module {
    single<SessionManager> {
        val context = getOrNull<Context>()
        if (context != null) {
            val sharedPrefs = context.getSharedPreferences(
                "supabase_session",
                Context.MODE_PRIVATE,
            )
            SettingsSessionManager(settings = SharedPreferencesSettings(delegate = sharedPrefs))
        } else {
            MemorySessionManager()
        }
    }
}
