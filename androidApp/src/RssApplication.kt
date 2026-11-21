package com.jjcr11.rss

import android.app.Application
import com.jjcr11.rss.di.initKoin
import org.koin.android.ext.koin.androidContext

class RssApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(androidContext = this@RssApplication)
        }
    }
}
