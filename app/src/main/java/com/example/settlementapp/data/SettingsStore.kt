package com.example.settlementapp.data

import android.content.Context
import com.example.settlementapp.ui.i18n.AppLanguage
import com.example.settlementapp.ui.i18n.AppCurrency

/** 앱 설정(언어 등)을 SharedPreferences 에 저장 */
class SettingsStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("settlement_settings", Context.MODE_PRIVATE)

    /** 사용자가 직접 선택하지 않았으면 시스템 언어를 반환 */
    fun getLanguage(): AppLanguage {
        if (!hasUserLanguagePreference()) {
            return AppLanguage.fromSystemLocale()
        }
        return AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, null))
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    fun hasUserLanguagePreference(): Boolean = prefs.contains(KEY_LANGUAGE)

    fun getCurrency(): AppCurrency = AppCurrency.fromCode(prefs.getString(KEY_CURRENCY, null))

    fun setCurrency(currency: AppCurrency) {
        prefs.edit().putString(KEY_CURRENCY, currency.code).apply()
    }

    companion object {
        private const val KEY_LANGUAGE = "language"
        private const val KEY_CURRENCY = "currency"
    }
}
