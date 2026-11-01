package com.lucent.app.harness

import android.content.Context
import com.lucent.app.ui.platformStorageAccessGranted
import com.lucent.app.ui.requestAndroidAllFilesAccess

object AndroidWorkspacePicker {

    fun storageGranted(context: Context? = null): Boolean =
        platformStorageAccessGranted(context)

    fun requestAllFilesAccess(context: Context) {
        requestAndroidAllFilesAccess(context)
    }
}
