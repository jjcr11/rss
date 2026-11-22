package com.jjcr11.rss.di

import com.russhwolf.settings.PreferencesSettings
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.SettingsSessionManager
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.prefs.Preferences

actual val platformSessionModule: Module = module {
    single<SessionManager> {
        val prefs = Preferences.userRoot().node("com.jjcr11.rss.auth")
        SettingsSessionManager(settings = PreferencesSettings(delegate = prefs))
    }
}
