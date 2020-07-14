package io.github.durun.pinner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.util.InternalAPI
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.Serializable

class CalendarEvent(
    private val title: String? = null,
    private val description: String? = null,
    private val image: Uri? = null
): Serializable {
    companion object {
        const val INTENT_KEY = "CalendarEvent"
    }

    /**
     * throws exception
     */
    @InternalAPI
    fun submit(context: Context) {
        val text = image
            ?.resolveImage(context)
            ?.uploadToImgur()?.plus("\n${description.orEmpty().trim()}")
            ?: description.orEmpty().trim()
        context.launchCalendar(title, description = text)
    }


    private fun Context.launchCalendar(title: String? = null, description: String? = null) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        title?.let { intent.putExtra(CalendarContract.Events.TITLE, it) }
        description?.let { intent.putExtra(CalendarContract.Events.DESCRIPTION, it) }
        this.startActivity(intent)
    }

    private fun Uri.resolveImage(context: Context): ByteArray? {
        return context.contentResolver.openInputStream(this)?.readBytes()
    }

    /**
     * throws exception
     */
    @InternalAPI
    private fun ByteArray.uploadToImgur(): String {
        return HttpClient(Android).use {
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
                        append("image", this@uploadToImgur)
                    })
                }
            }
            JSONObject(response).getJSONObject("data").getString("link")
        }
    }
}