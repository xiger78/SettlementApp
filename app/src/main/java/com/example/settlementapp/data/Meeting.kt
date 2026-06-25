package com.example.settlementapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 모임 정보 (정산 단위)
 * - 모임날짜, 가게이름, 가게전화번호, 참가인원(남/여), 정산금액, 여자금액, 남자금액, 기타내용
 */
@Entity(tableName = "meetings")
data class Meeting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 모임날짜 (yyyy-MM-dd) */
    val meetingDate: String,

    /** 가게이름 */
    val storeName: String = "",

    /** 가게전화번호 */
    val storePhone: String = "",

    /** 참가인원 */
    val totalCount: Int = 0,

    /** 남자 인원 */
    val maleCount: Int = 0,

    /** 여자 인원 */
    val femaleCount: Int = 0,

    /** 정산금액 (영수증 총액) */
    val settlementAmount: Long = 0,

    /** 여자 1인 금액 */
    val femaleAmount: Long = 0,

    /** 남자 1인 금액 */
    val maleAmount: Long = 0,

    /** 남여차이 금액 (남자가 여자보다 더 내는 1인 차액, 0이면 미사용) */
    val genderDiffAmount: Long = 0,

    /** 기타내용 */
    val note: String = "",

    /** 영수증 사진 경로 (content uri 문자열) */
    val receiptPhotoUri: String? = null
)
