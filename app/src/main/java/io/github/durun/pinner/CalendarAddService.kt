package io.github.durun.pinner

import android.app.IntentService
import android.content.Intent
import android.util.Log
import io.ktor.util.InternalAPI

class CalendarAddService : IntentService(CalendarAddService::class.simpleName) {
    companion object {
        private val TAG = CalendarAddService::class.java.simpleName
    }

    @InternalAPI
    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "received intent: $intent")

        val event = intent?.getSerializableExtra(CalendarEvent.INTENT_KEY) as? CalendarEvent
            ?: return

        runCatching {
            event.submit(context = application)
        }.onFailure {
            Log.d(TAG, it.toString())
        }
    }
}