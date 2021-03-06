package io.github.durun.pinner

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.ktor.util.InternalAPI

class CalendarAddService : Service() {
    companion object {
        private val TAG = CalendarAddService::class.java.simpleName
        private val CHANNEL_ID = TAG
    }

    @InternalAPI
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "received: $intent")

        val event = intent?.getParcelableExtra<CalendarEvent>(CalendarEvent.INTENT_KEY)
            ?: return super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "calendarEvent: $event")

        // prepare notification
        val progressNotification = createNotification()
            .apply {
                second
                    .setContentText("In progress")
                    .setProgress(0, 0, true)
            }
        createNotificationChannel()
        // show progress notification
        progressNotification.notify()

        // call calendar
        Thread(Runnable {
            runCatching {
                Log.d(TAG, "submitting event")
                val i = event.toIntent(context = this)
                Log.d(TAG, "launchCalendar: $i")
                startActivity(i)
            }.onFailure {
                Log.d(TAG, it.toString())
                val failNotification = createNotification().apply {
                    val i = Intent(this@CalendarAddService, CalendarAddService::class.java)
                        .putExtra(CalendarEvent.INTENT_KEY, event)
                    second
                        .setContentText("Failed.\nTap to retry.")
                        .setContentIntent(
                            PendingIntent.getService(
                                this@CalendarAddService,
                                0,
                                i,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        )
                }
                failNotification.notify()
            }.onSuccess {
                NotificationManagerCompat.from(this)
                    .cancelAll()
            }
            cacheDir.deleteRecursively()
            Log.d(TAG, "Deleted cache.")
            progressNotification.cancel()
        }).start()
        Log.d(TAG, "return.")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    var notificationId = 0
    private fun createNotification(): Pair<Int, NotificationCompat.Builder> {
        return (notificationId++) to NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.pinner_foreground)
            setContentTitle("Pinner")
            priority = NotificationCompat.PRIORITY_LOW
        }
    }

    private fun Pair<Int, NotificationCompat.Builder>.notify() {
        NotificationManagerCompat.from(this@CalendarAddService)
            .notify(this.first, this.second.build())
    }

    private fun Pair<Int, NotificationCompat.Builder>.cancel() {
        NotificationManagerCompat.from(this@CalendarAddService)
            .cancel(this.first)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pinner"
            val descriptionText = "Pinner notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}