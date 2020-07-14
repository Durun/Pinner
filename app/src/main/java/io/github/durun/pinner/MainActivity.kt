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

        val event = intent.toCalendarEvent()
        event?.let {
            val i = Intent(this, CalendarAddService::class.java)
                .putExtra(CalendarEvent.INTENT_KEY, it)
            startService(i)
        }

        finish()
    }

    private fun Intent.toCalendarEvent(): CalendarEvent? {
        return when (this.action) {
            Intent.ACTION_SEND -> {
                when (true) {
                    this.type?.startsWith("text/") -> {
                        val text = this.getStringExtra(Intent.EXTRA_TEXT)
                        CalendarEvent(
                            title = text?.take(TRIM_SIZE_TITLE),
                            description = text
                        )
                    }
                    this.type?.startsWith("image/") -> {
                        val imageUri =
                            (this.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)
                        CalendarEvent(
                            image = imageUri?.resolveImage()
                        )
                    }
                    else -> null
                }
            }
            else -> null
        }
    }

    private fun Uri.resolveImage(): ByteArray? {
        return contentResolver.openInputStream(this)?.readBytes()
    }
}