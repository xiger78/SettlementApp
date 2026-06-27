package com.example.settlementapp.ui.i18n

enum class AppCurrency(val code: String, val suffix: String, val prefix: String) {
    KRW("krw", "원", ""),
    JPY("jpy", "円", ""),
    USD("usd", "", "$"),
    CNY("cny", "元", "");

    fun format(value: Long): String {
        val num = groupNumber(value)
        return if (prefix.isNotEmpty()) "$prefix$num" else "$num$suffix"
    }

    companion object {
        fun fromCode(code: String?): AppCurrency =
            entries.firstOrNull { it.code == code } ?: KRW
    }
}
