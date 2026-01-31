package org.example.despertador

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun createDataStore(context: Any?): DataStore<Preferences> {
    // Adiciona uma verificação para garantir que o contexto não seja nulo no Android.
    requireNotNull(context) { "Context must be provided on Android" }
    val androidContext = context as Context

    return PreferenceDataStoreFactory.create(
        // CORREÇÃO: O DataStore no Android espera um java.io.File, e a função 'resolve' já retorna isso.
        // A conversão para okio.Path é necessária apenas para outras plataformas, como o iOS.
        produceFile = { androidContext.filesDir.resolve("alarms.preferences_pb") }
    )
}
