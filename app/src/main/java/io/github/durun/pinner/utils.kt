package io.github.durun.pinner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.provider.CalendarContract
import android.util.Log
import androidx.core.net.toUri
import java.io.InputStream

internal fun Uri.resolveImage(context: Context): InputStream? {
    return context.contentResolver.openInputStream(this)
}

internal fun Uri.asMyUrl(context: Context): Uri {
    return context.contentResolver.openInputStream(this).use { input ->
        val file = context.cacheDir.resolve(this.hashCode().toString())
        file.takeIf { it.exists() }?.delete()
        file.createNewFile()
        input?.copyTo(file.outputStream())
        file.toUri()
    }
}

internal fun Intent.toCalendarEvent(context: Context): CalendarEvent? {
    return when (this.action) {
        Intent.ACTION_SEND -> {
            when (true) {
                this.type?.startsWith("text/") -> {
                    val text = this.getStringExtra(Intent.EXTRA_TEXT)
                    CalendarEvent(
                        title = text?.take(MainActivity.TRIM_SIZE_TITLE),
                        description = text
                    )
                }
                this.type?.startsWith("image/") -> {
                    val imageUri =
                        (this.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)
                            ?.asMyUrl(context)
                    CalendarEvent(
                        image = imageUri
                    )
                }
                else -> null
            }
        }
        else -> null
    }
}

internal fun calendarIntentOf(title: String? = null, description: String? = null): Intent {
    val intent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    title?.let { intent.putExtra(CalendarContract.Events.TITLE, it) }
    description?.let { intent.putExtra(CalendarContract.Events.DESCRIPTION, it) }
    Log.d("", "calendar: title=$title, desc=$description")
    return intent
}