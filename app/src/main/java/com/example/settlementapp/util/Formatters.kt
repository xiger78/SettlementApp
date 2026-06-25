package com.example.settlementapp.util

/** "1,234" 또는 "1234" 형태 문자열에서 숫자만 추출 */
fun String.toAmountLong(): Long =
    filter { it.isDigit() }.toLongOrNull() ?: 0L
