package com.example.settlementapp.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromGender(value: Gender): String = value.name

    @TypeConverter
    fun toGender(value: String): Gender = runCatching { Gender.valueOf(value) }.getOrDefault(Gender.MALE)

    @TypeConverter
    fun fromPaymentType(value: PaymentType): String = value.name

    @TypeConverter
    fun toPaymentType(value: String): PaymentType =
        runCatching { PaymentType.valueOf(value) }.getOrDefault(PaymentType.CASH)
}
