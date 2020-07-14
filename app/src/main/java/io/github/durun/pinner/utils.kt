package io.github.durun.pinner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import java.io.InputStream

internal fun Uri.resolveImage(context: Context): InputStream? {
    return context.contentResolver.openInputStream(this)
}

internal fun Uri.asMyUrl(context: Context): Uri {
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