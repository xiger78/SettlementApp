package com.example.settlementapp.data

import androidx.room.Embedded
import androidx.room.Relation

data class MeetingWithParticipants(
    @Embedded val meeting: Meeting,
    @Relation(
        parentColumn = "id",
        entityColumn = "meetingId"
    )
    val participants: List<Participant>
)

/** 월별 정산 합계 (월별정산일람용) */
data class MonthlySummary(
    val month: String,            // yyyy-MM
    val meetingCount: Int,        // 모임 수
    val totalAmount: Long,        // 정산금액 합계
    val totalParticipants: Int    // 총 참가 인원
)
