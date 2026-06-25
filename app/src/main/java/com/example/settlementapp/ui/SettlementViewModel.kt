package com.example.settlementapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.settlementapp.data.Gender
import com.example.settlementapp.data.Meeting
import com.example.settlementapp.data.MeetingWithParticipants
import com.example.settlementapp.data.MonthlySummary
import com.example.settlementapp.data.Participant
import com.example.settlementapp.data.PaymentType
import com.example.settlementapp.data.SettlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettlementViewModel(
    private val repository: SettlementRepository
) : ViewModel() {

    val meetings: StateFlow<List<Meeting>> =
        repository.observeMeetings()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySummary: StateFlow<List<MonthlySummary>> =
        repository.observeMonthlySummary()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun meetingFlow(id: Long): Flow<Meeting?> = repository.observeMeeting(id)

    fun participantsFlow(id: Long): Flow<List<Participant>> = repository.observeParticipants(id)

    fun meetingWithParticipantsFlow(id: Long): Flow<MeetingWithParticipants?> =
        repository.observeMeetingWithParticipants(id)

    // ---- 모임정보 등록/수정 ----
    fun saveMeeting(meeting: Meeting, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (meeting.id == 0L) {
                repository.insertMeeting(meeting)
            } else {
                repository.updateMeeting(meeting)
                meeting.id
            }
            onSaved(id)
        }
    }

    fun deleteMeeting(id: Long) {
        viewModelScope.launch { repository.deleteMeeting(id) }
    }

    fun setReceiptPhoto(meetingId: Long, uri: String?) {
        viewModelScope.launch {
            repository.getMeeting(meetingId)?.let {
                repository.updateMeeting(it.copy(receiptPhotoUri = uri))
            }
        }
    }

    // ---- 참가자 등록/수정 ----
    fun addParticipant(meetingId: Long, name: String, gender: Gender, paymentType: PaymentType) {
        viewModelScope.launch {
            repository.insertParticipant(
                Participant(
                    meetingId = meetingId,
                    name = name.trim(),
                    gender = gender,
                    paymentType = paymentType
                )
            )
            syncMeetingHeadcount(meetingId)
        }
    }

    fun updateParticipant(participant: Participant) {
        viewModelScope.launch { repository.updateParticipant(participant) }
    }

    fun deleteParticipant(participant: Participant) {
        viewModelScope.launch {
            repository.deleteParticipant(participant)
            syncMeetingHeadcount(participant.meetingId)
        }
    }

    /** 참가자 명단 기준으로 모임의 인원수(총/남/여)를 갱신 */
    private suspend fun syncMeetingHeadcount(meetingId: Long) {
        val meeting = repository.getMeeting(meetingId) ?: return
        val list = repository.getParticipants(meetingId)
        val male = list.count { it.gender == Gender.MALE }
        val female = list.count { it.gender == Gender.FEMALE }
        repository.updateMeeting(
            meeting.copy(totalCount = list.size, maleCount = male, femaleCount = female)
        )
    }

    // ---- 정산 ----
    /**
     * 정산완료 처리.
     * - femaleAmount: 여자 1인 금액 (입력값)
     * - settlementAmount: 정산(영수증) 총액
     * - maleAmount: (총액 - 여자총액) / 남자수 로 자동 계산
     */
    fun completeSettlement(
        meetingId: Long,
        settlementAmount: Long,
        femaleAmount: Long,
        participantsUi: List<Participant>
    ) {
        viewModelScope.launch {
            val meeting = repository.getMeeting(meetingId) ?: return@launch
            val maleCount = participantsUi.count { it.gender == Gender.MALE }
            val femaleCount = participantsUi.count { it.gender == Gender.FEMALE }
            val maleAmount = computeMaleAmount(settlementAmount, femaleAmount, femaleCount, maleCount)

            repository.updateMeeting(
                meeting.copy(
                    settlementAmount = settlementAmount,
                    femaleAmount = femaleAmount,
                    maleAmount = maleAmount,
                    totalCount = participantsUi.size,
                    maleCount = maleCount,
                    femaleCount = femaleCount
                )
            )

            val updated = participantsUi.map { p ->
                val amount = if (p.gender == Gender.FEMALE) femaleAmount else maleAmount
                p.copy(amount = amount)
            }
            repository.updateParticipants(updated)
        }
    }

    companion object {
        /** 남자 1인 금액 = (정산총액 - 여자총액) / 남자수 */
        fun computeMaleAmount(
            settlementAmount: Long,
            femaleAmount: Long,
            femaleCount: Int,
            maleCount: Int
        ): Long {
            if (maleCount <= 0) return 0
            val femaleTotal = femaleAmount * femaleCount
            val remaining = (settlementAmount - femaleTotal).coerceAtLeast(0)
            return remaining / maleCount
        }
    }
}

class SettlementViewModelFactory(
    private val repository: SettlementRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettlementViewModel::class.java)) {
            return SettlementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
