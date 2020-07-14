package io.github.durun.pinner

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import io.ktor.util.InternalAPI

class CalendarAddService : Service() {
    companion object {
        private val TAG = CalendarAddService::class.java.simpleName
    }

    @InternalAPI
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(TAG, "received intent: $intent")

        val event = intent?.getParcelableExtra<CalendarEvent>(CalendarEvent.INTENT_KEY)
            ?: return START_STICKY

        Thread(
            Runnable {
                runCatching {
                    event.submit(context = application)
                }.onFailure {
                    Log.d(TAG, it.toString())
                }
                stopSelf()
            }).start()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}