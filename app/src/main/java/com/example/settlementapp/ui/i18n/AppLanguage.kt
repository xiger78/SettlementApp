package com.example.settlementapp.ui.i18n

import java.util.Locale

enum class AppLanguage(val code: String, val nativeName: String) {
    KOREAN("ko", "한국어"),
    JAPANESE("ja", "日本語"),
    ENGLISH("en", "English"),
    CHINESE("zh", "中文");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: fromSystemLocale()

        /** 휴대폰 시스템 언어에 맞는 앱 언어 (미지원 언어는 English) */
        fun fromSystemLocale(): AppLanguage {
            val lang = Locale.getDefault().language.lowercase()
            return when (lang) {
                "ko" -> KOREAN
                "ja" -> JAPANESE
                "zh" -> CHINESE
                "en" -> ENGLISH
                else -> ENGLISH
            }
        }
    }
}
