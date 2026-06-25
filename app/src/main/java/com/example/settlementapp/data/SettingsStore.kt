package com.example.settlementapp.data

import android.content.Context
import com.example.settlementapp.ui.i18n.AppLanguage

/** 앱 설정(언어 등)을 SharedPreferences 에 저장 */
class SettingsStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("settlement_settings", Context.MODE_PRIVATE)

    fun getLanguage(): AppLanguage =
        AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, null))

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    companion object {
        private const val KEY_LANGUAGE = "language"
    }
}
