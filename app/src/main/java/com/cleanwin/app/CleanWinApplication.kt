package com.cleanwin.app

import android.app.Application
import com.cleanwin.app.data.local.AppDatabase

class CleanWinApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getInstance(this)
    }
}
