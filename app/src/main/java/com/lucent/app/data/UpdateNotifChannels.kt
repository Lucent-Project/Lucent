package com.lucent.app.data

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.lucent.app.i18n.S
import com.lucent.app.i18n.currentLanguageKey
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.runBlocking

object UpdateNotifChannels {

    private val ensuredLangs = ConcurrentHashMap<String, String>()

    fun ensureLocalizedChannel(context: Context, channelId: String, importance: Int, withDescription: Boolean) {
        val manager = NotificationManagerCompat.from(context)
        val currentKey = currentLanguageKey()
        if (ensuredLangs[channelId] != currentKey) {
            val stored = runCatching {
                runBlocking { createSettingsRepository(context).notificationChannelLangOnce(channelId) }
            }.getOrDefault("")
            if (UpdateChannelLocale.needsRecreate(stored, currentKey)) {
                manager.deleteNotificationChannel(channelId)
                runCatching {
                    runBlocking { createSettingsRepository(context).setNotificationChannelLang(channelId, currentKey) }
                }
            }
            ensuredLangs[channelId] = currentKey
        }
        val builder = NotificationChannelCompat.Builder(channelId, importance)
            .setName(S.updateChannelName)
        if (withDescription) builder.setDescription(S.updateChannelDesc)
        manager.createNotificationChannel(builder.build())
    }
}
