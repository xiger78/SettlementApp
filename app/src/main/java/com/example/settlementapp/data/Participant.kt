package com.example.settlementapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Gender(val label: String) {
    MALE("남자"),
    FEMALE("여자");

    companion object {
        fun fromLabel(label: String): Gender = entries.firstOrNull { it.label == label } ?: MALE
    }
}

enum class PaymentType(val label: String) {
    CASH("현금"),
    PAYPAY("페이페이");

    companion object {
        fun fromLabel(label: String): PaymentType = entries.firstOrNull { it.label == label } ?: CASH
    }
}

/**
 * 참가자 정보
 * - 모임날짜(meetingId 로 연결), 이름, 성별, 정산형태(현금/페이페이), 금액, 정산완료 여부, 기타내용
 */
@Entity(
    tableName = "participants",
    foreignKeys = [
        ForeignKey(
            entity = Meeting::class,
            parentColumns = ["id"],
            childColumns = ["meetingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("meetingId")]
)
data class Participant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 소속 모임 id */
    val meetingId: Long,

    /** 이름 */
    val name: String,

    /** 성별 */
    val gender: Gender = Gender.MALE,

    /** 정산형태 (현금 / 페이페이) */
    val paymentType: PaymentType = PaymentType.CASH,

    /** 청구 금액 (정산 화면 계산값) */
    val amount: Long = 0,

    /** 정산 완료 여부 */
    val isSettled: Boolean = false,

    /** 기타내용 */
    val note: String = ""
)
