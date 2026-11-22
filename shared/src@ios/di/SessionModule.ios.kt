package com.jjcr11.rss.di

import com.russhwolf.settings.NSUserDefaultsSettings
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.SettingsSessionManager
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val platformSessionModule: Module = module {
    single<SessionManager> {
        val defaults = NSUserDefaults.standardUserDefaults
        SettingsSessionManager(settings = NSUserDefaultsSettings(delegate = defaults))
    }
}
