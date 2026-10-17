package com.lucent.app.ui

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.net.toUri
import com.lucent.app.i18n.S

actual object LucentBrowser {
    private const val ACTION_OPEN_DEFAULT = "com.lucent.app.action.OPEN_IN_DEFAULT_BROWSER"
    private const val ACTION_COPY_LINK = "com.lucent.app.action.COPY_LINK"
    private const val RECEIVER_CLASS = "com.lucent.app.ui.BrowserMenuReceiver"

    private var appContext: Context? = null

    fun init(context: Context) {
        val app = context.applicationContext
        appContext = app
        val pkg = CustomTabsClient.getPackageName(app, null)
        if (pkg != null) {
            warmup(app, pkg)
        }
    }

    private fun resolveContext(): Context? =
        appContext ?: ActivityProvider.currentActivity?.applicationContext ?: runCatching {
            Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null) as? Context
        }.getOrNull()

    private fun plainActionView(ctx: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        } catch (_: Throwable) {}
    }

    private fun warmup(ctx: Context, pkg: String) {
        try {
            val connection = object : CustomTabsServiceConnection() {
                override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                    try {
                        client.warmup(0)
                        client.newSession(null)?.mayLaunchUrl(null, null, null)
                    } catch (_: Throwable) {
                    } finally {
                        try {
                            ctx.unbindService(this)
                        } catch (_: Throwable) {}
                    }
                }

                override fun onServiceDisconnected(name: ComponentName) {}
            }
            CustomTabsClient.bindCustomTabsService(ctx, pkg, connection)
        } catch (_: Throwable) {}
    }

    private fun pendingIntentFor(ctx: Context, requestCode: Int, action: String, uri: Uri): PendingIntent {
        val intent = Intent(action).apply {
            setClassName(ctx.packageName, RECEIVER_CLASS)
            data = uri
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
        return PendingIntent.getBroadcast(ctx, requestCode, intent, flags)
    }

    actual fun openUrl(url: String) {
        val ctx = resolveContext() ?: return

        val uri = try {
            url.toUri()
        } catch (_: Throwable) {
            return
        }

        if (appContext == null) {
            plainActionView(ctx, uri)
            return
        }

        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            plainActionView(ctx, uri)
            return
        }

        val pkg = CustomTabsClient.getPackageName(ctx, null)
        if (pkg == null) {
            plainActionView(ctx, uri)
            return
        }

        warmup(ctx, pkg)

        try {
            val toolbarColor = 0xFF1B2340.toInt()
            val colorSchemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(toolbarColor)
                .build()

            val builder = CustomTabsIntent.Builder()
            @Suppress("DEPRECATION")
            builder.setToolbarColor(toolbarColor)
            builder.setDefaultColorSchemeParams(colorSchemeParams)
            builder.setShareState(CustomTabsIntent.SHARE_STATE_ON)
            builder.addMenuItem(S.browserOpenInDefault, pendingIntentFor(ctx, 1, ACTION_OPEN_DEFAULT, uri))
            builder.addMenuItem(S.browserCopyLink, pendingIntentFor(ctx, 2, ACTION_COPY_LINK, uri))
            builder.setStartAnimations(ctx, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            builder.setExitAnimations(ctx, android.R.anim.slide_in_left, android.R.anim.slide_out_right)

            val customTabsIntent = builder.build()
            customTabsIntent.intent.setPackage(pkg)
            if (ctx !is Activity) {
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            customTabsIntent.launchUrl(ctx, uri)
        } catch (_: Throwable) {
            plainActionView(ctx, uri)
        }
    }
}
