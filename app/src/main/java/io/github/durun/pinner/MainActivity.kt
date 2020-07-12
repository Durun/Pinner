package io.github.durun.pinner

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.CalendarContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                }else if(intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent)
                }
            }
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }
    }
    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            // Update UI to reflect text being shared
            Log.d(TAG, "received: $it")

            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, it.take(10))
                .putExtra(CalendarContract.Events.DESCRIPTION, it)

            Log.d(TAG, "intent: $intent")
            startActivity(intent)
        }
    }
    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            // Update UI to reflect image being shared
            Log.d(TAG, "received: $it")
            val bytes = contentResolver.openInputStream(it)?.readBytes()
            Log.d(TAG, "bytes: ${bytes?.take(10)}")
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}