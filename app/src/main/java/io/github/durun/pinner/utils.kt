package io.github.durun.pinner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable

internal fun Uri.resolveImage(context: Context): ByteArray? {
    return context.contentResolver.openInputStream(this)?.readBytes()
}

internal fun Intent.toCalendarEvent(): CalendarEvent? {
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