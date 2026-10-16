package com.lucent.app

object LucentBuild {

    const val VERSION = "3.1.2"

    const val BUILD_NUMBER = "311"
    
    val BUILD_ID: String = GeneratedBuildId.BUILD_ID
    
    val BUILD_TRACK: String = when (BUILD_ID.firstOrNull()) {
        'P' -> "preview"
        'R' -> "official"
        else -> "dev"
    }

    val DISPLAY_VERSION: String = if (BUILD_TRACK == "preview") "$VERSION-alpha" else VERSION

    const val PRODUCT_NAME = "Lucent"

    const val COPYRIGHT = "Copyright \u00A9 2026-2027 Jessica Martinez"

    const val DEVELOPER = "Jessica Martinez"

    const val SUPPORT_EMAIL = "yuan47578@gmail.com"

    const val HOMEPAGE = "https://github.com/Yuan0-o/Lucent"

    const val LICENSES_PAGE = "https://github.com/Yuan0-o/Lucent/blob/main/docs/THIRD-PARTY-NOTICES.md"

    const val RELEASES_API = "https://api.github.com/repos/Yuan0-o/Lucent/releases/latest"

    const val REPO_API = "https://api.github.com/repos/Yuan0-o/Lucent"

    private const val PRIVACY_PAGE = "https://yuan0-o.github.io/lucentapp"

    fun privacyPage(languageKey: String): String = when (languageKey) {
        "zh" -> "$PRIVACY_PAGE/zh/privacy.html"
        "ja" -> "$PRIVACY_PAGE/ja/privacy.html"
        "ko" -> "$PRIVACY_PAGE/ko/privacy.html"
        else -> "$PRIVACY_PAGE/privacy.html"
    }
}
