package io.github.durun.pinner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import io.ktor.util.InternalAPI

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
        const val TRIM_SIZE_TITLE = 50
    }

    @InternalAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val event = intent.toCalendarEvent(this)
        event?.let {
            val i = Intent(this, CalendarAddService::class.java)
                .setAction(Intent.ACTION_SEND)
                .putExtra(CalendarEvent.INTENT_KEY, it)
            startService(i)
        }
        finish()
    }
}