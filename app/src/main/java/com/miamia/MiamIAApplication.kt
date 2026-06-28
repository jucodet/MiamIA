package com.miamia

import android.app.Application
import androidx.room.Room
import com.miamia.data.db.AppDatabase
import com.miamia.ingredientknowledge.EmbeddedReferenceKb
import com.miamia.ingredientknowledge.FileKbCacheStore
import com.miamia.ingredientknowledge.KbRefreshConfig
import com.miamia.ingredientknowledge.KbRefreshCoordinator
import com.miamia.ingredientknowledge.OffCiqualRefreshGateway
import com.miamia.ingredientknowledge.RefreshableReferenceKb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Point d’entrée process : la base Room est ouverte **lazy** (premier accès, hors thread UI recommandé)
 * pour éviter un blocage long dans [android.app.Activity.onCreate].
 *
 * IKB-B : la base référence additifs est rafraîchie **au démarrage** de façon **non bloquante**
 * (fire-and-forget sur `Dispatchers.IO`). L'index courant (cache `filesDir` → à défaut baseline
 * embarquée) reste explovable immédiatement ; la version rafraîchie est publiée par swap atomique
 * quand le refresh aboutit (IKB-B-FR-002, IKB-B-SC-006).
 */
class MiamIAApplication : Application() {

    val database: AppDatabase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "miamia.db").build()
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val kbCacheStore: FileKbCacheStore by lazy { FileKbCacheStore(applicationContext) }

    private val embeddedBaseline: EmbeddedReferenceKb by lazy { EmbeddedReferenceKb.load(applicationContext) }

    /**
     * Base référence exposée au core : version courante disponible (cache → baseline) + refresh différé.
     */
    val referenceKb: RefreshableReferenceKb by lazy {
        RefreshableReferenceKb(
            cacheStore = kbCacheStore,
            baseline = embeddedBaseline,
        )
    }

    private val kbRefreshCoordinator: KbRefreshCoordinator by lazy {
        KbRefreshCoordinator(
            gateway = OffCiqualRefreshGateway(
                baselineAllergens = embeddedBaseline.allergens,
                config = KbRefreshConfig(),
            ),
            cacheStore = kbCacheStore,
            baseline = embeddedBaseline,
            onRefreshed = referenceKb::publishRefreshed,
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Refresh non bloquant : l'UI démarre immédiatement, le refresh publie la nouvelle version
        // par swap atomique lorsqu'il aboutit (repli cache → baseline en cas d'échec réseau).
        appScope.launch { kbRefreshCoordinator.refreshAtStartup() }
    }
}
