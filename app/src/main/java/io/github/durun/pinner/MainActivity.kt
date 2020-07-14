package io.github.durun.pinner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.util.InternalAPI
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
        const val TRIM_SIZE_TITLE = 50
    }

    @InternalAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val event = intent?.toCalendarEvent() ?: return finish()

        runCatching {
            event.submit(context = this)
        }.onFailure {
            Log.d(TAG, it.toString())
            Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show()
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
                        val imageData = (this.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                            contentResolver.openInputStream(it)?.readBytes()
                        }
                        CalendarEvent(
                            imageData = imageData
                        )
                    }
                    else -> null
                }
            }
            else -> null
        }
    }
}