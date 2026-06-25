package com.example.settlementapp.util

import java.text.NumberFormat
import java.util.Locale

private val won: NumberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

/** 1234567 -> "1,234,567원" */
fun Long.toWon(): String = "${won.format(this)}원"

fun Int.toWon(): String = "${won.format(this)}원"

/** "1,234" 또는 "1234" 형태 문자열에서 숫자만 추출 */
fun String.toAmountLong(): Long =
    filter { it.isDigit() }.toLongOrNull() ?: 0L

/** yyyy-MM -> "2026년 6월" */
fun monthLabel(yyyymm: String): String {
    val parts = yyyymm.split("-")
    if (parts.size < 2) return yyyymm
    val month = parts[1].toIntOrNull() ?: return yyyymm
    return "${parts[0]}년 ${month}월"
}
