package com.lucent.app.data

object UpdateChannelLocale {

    fun needsRecreate(storedLangKey: String, currentLangKey: String): Boolean =
        storedLangKey != currentLangKey
}
