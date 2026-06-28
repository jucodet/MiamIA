package com.miamia.analysis

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.miamia.R

/**
 * Service de premier plan maintenu actif pendant l'inférence Gemma locale (composition / critique
 * santé) afin que le système ne tue pas le processus lorsque l'application perd le focus (mise en
 * arrière-plan, rotation, etc.). L'inférence elle-même s'exécute dans le `viewModelScope` ; ce
 * service se contente de porter une notification persistante pour protéger le processus.
 */
class AnalysisForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START) {
            startForegroundNotification()
        }
        return START_NOT_STICKY
    }

    private fun startForegroundNotification() {
        ensureChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MiamIA")
            .setContentText("Analyse des ingrédients en cours…")
            .setSmallIcon(R.drawable.ic_analysis_notification)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Analyse en cours",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification affichée pendant l'analyse des ingrédients."
                setShowBadge(false)
            }
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.miamia.analysis.START"
        private const val CHANNEL_ID = "analysis_in_progress"
        private const val NOTIFICATION_ID = 4242

        fun start(context: Context) {
            val intent = Intent(context, AnalysisForegroundService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            // stopService (et non startService) reste autorisé quand l'application est en
            // arrière-plan : la restriction API 26+ ne s'applique qu'au démarrage de services.
            context.stopService(Intent(context, AnalysisForegroundService::class.java))
        }
    }
}
