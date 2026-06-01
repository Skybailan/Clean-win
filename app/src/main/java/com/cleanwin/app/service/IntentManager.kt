package com.cleanwin.app.service

import android.app.PendingIntent
import java.util.concurrent.ConcurrentHashMap

object IntentManager {
    private val intentCache = ConcurrentHashMap<String, PendingIntent>()

    fun saveIntent(key: String, intent: PendingIntent) {
        intentCache[key] = intent
    }

    fun getIntent(key: String): PendingIntent? {
        return intentCache[key]
    }
    
    fun isIntentAvailable(key: String): Boolean {
        return intentCache.containsKey(key)
    }

    fun removeIntent(key: String) {
        intentCache.remove(key)
    }
}
