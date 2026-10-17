package com.lucent.app.ui

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

class BrowserMenuReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_OPEN_DEFAULT = "com.lucent.app.action.OPEN_IN_DEFAULT_BROWSER"
        const val ACTION_COPY_LINK = "com.lucent.app.action.COPY_LINK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val url = intent.dataString ?: return
        when (intent.action) {
            ACTION_COPY_LINK -> {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.setPrimaryClip(ClipData.newPlainText("URL", url))
            }
            else -> {
                try {
                    val viewIntent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(viewIntent)
                } catch (_: Throwable) {}
            }
        }
    }
}
