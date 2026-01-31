package org.example.despertador

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun createDataStore(context: Any? = null): DataStore<Preferences>