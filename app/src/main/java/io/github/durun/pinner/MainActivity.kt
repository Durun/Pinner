package io.github.durun.pinner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.CalendarContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.headers
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                } else if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent)
                }
            }
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }
    }

    fun addCalendarEvent(title: String? = null, description: String? = null) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
        title?.let { intent.putExtra(CalendarContract.Events.TITLE, it) }
        description?.let { intent.putExtra(CalendarContract.Events.DESCRIPTION, it) }
        startActivity(intent)
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            // Update UI to reflect text being shared
            Log.d(TAG, "received: $it")
            addCalendarEvent(title = it.take(16), description = it)
        }
    }

    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            // Update UI to reflect image being shared
            Log.d(TAG, "received: $it")
            val bytes = contentResolver.openInputStream(it)?.readBytes()
            Log.d(TAG, "bytes: ${bytes?.take(10)}")

            HttpClient(Android).use {
                val response = runBlocking {
                    it.post<String>(
                        scheme = "https",
                        host = "api.imgur.com",
                        path = "/3/image"
                    ) {
                        headers {
                            append("authorization", "Client-ID 9040b7b183b6471")
                        }
                        body = MultiPartFormDataContent(formData {
                            append("image", bytes!!)
                        })
                    }
                }
                Log.d(TAG, "response: $response")
                val link = JSONObject(response).getJSONObject("data").getString("link")
                Log.d(TAG, link)
                addCalendarEvent(description = link)
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}