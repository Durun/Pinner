package io.github.durun.pinner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.util.InternalAPI
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.InputStream

class CalendarEvent(
    private val title: String? = null,
    private val description: String? = null,
    private val image: Uri? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Uri::class.java.classLoader)
    )
    companion object {
        private val TAG = CalendarEvent::class.java.simpleName
        const val INTENT_KEY = "CalendarEvent"

        @JvmField
        val CREATOR = object : Parcelable.Creator<CalendarEvent> {
            override fun createFromParcel(parcel: Parcel): CalendarEvent {
                return CalendarEvent(parcel)
            }

            override fun newArray(size: Int): Array<CalendarEvent?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun describeContents(): Int = 0
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeParcelable(image, flags)
    }

    override fun toString(): String = "${this::class.simpleName} {$title, $description, $image}"

    /**
     * throws exception
     */
    @InternalAPI
    fun toIntent(context: Context): Intent {
        val text = image
            ?.resolveImage(context)?.use { input ->
                input.uploadToImgur() + "\n${description.orEmpty().trim()}"
            } ?: description.orEmpty().trim()

        return calendarIntentOf(title, description = text)
    }

    /**
     * throws exception
     */
    @InternalAPI
    private fun InputStream.uploadToImgur(): String {
        return HttpClient(Android).use {
            Log.d(TAG, "uploading")
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
                        appendInput("image") { this@uploadToImgur.asInput() }
                    })
                }
            }
            Log.d(TAG, "response: $response")
            JSONObject(response).getJSONObject("data").getString("link")
        }
    }
}