package com.miamia

import android.app.Application
import androidx.room.Room
import com.miamia.data.db.AppDatabase

/**
 * Point d’entrée process : la base Room est ouverte **lazy** (premier accès, hors thread UI recommandé)
 * pour éviter un blocage long dans [android.app.Activity.onCreate].
 */
class MiamIAApplication : Application() {

    val database: AppDatabase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "miamia.db").build()
    }
}
