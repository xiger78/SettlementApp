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
import com.example.settlementapp.data.SettingsStore
import com.example.settlementapp.data.SettlementRepository
import com.example.settlementapp.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettlementViewModel(
    private val repository: SettlementRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    private val _language = MutableStateFlow(settingsStore.getLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        settingsStore.setLanguage(language)
        _language.value = language
    }

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
     * - 여자 금액 미입력: 총액을 참가 인원 수로 균등 분배 (남녀 동일)
     * - 여자 금액 입력: 여자 1인 금액 적용, 나머지를 남자 인원 수로 분배
     */
    fun completeSettlement(
        meetingId: Long,
        settlementAmount: Long,
        femaleAmountInput: Long,
        femaleAmountEntered: Boolean,
        participantsUi: List<Participant>
    ) {
        viewModelScope.launch {
            val meeting = repository.getMeeting(meetingId) ?: return@launch
            val maleCount = participantsUi.count { it.gender == Gender.MALE }
            val femaleCount = participantsUi.count { it.gender == Gender.FEMALE }
            val calc = computeSettlement(
                settlementAmount, femaleAmountInput, femaleCount, maleCount, femaleAmountEntered
            )

            repository.updateMeeting(
                meeting.copy(
                    settlementAmount = settlementAmount,
                    femaleAmount = calc.femalePerPerson,
                    maleAmount = calc.malePerPerson,
                    totalCount = participantsUi.size,
                    maleCount = maleCount,
                    femaleCount = femaleCount
                )
            )

            val updated = participantsUi.map { p ->
                val amount = if (p.gender == Gender.FEMALE) calc.femalePerPerson else calc.malePerPerson
                p.copy(amount = amount)
            }
            repository.updateParticipants(updated)
        }
    }

    /** 정산 금액·완료 상태를 초기화 (영수증 사진은 유지) */
    fun resetSettlement(meetingId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val meeting = repository.getMeeting(meetingId) ?: return@launch
            repository.updateMeeting(
                meeting.copy(settlementAmount = 0, femaleAmount = 0, maleAmount = 0)
            )
            val participants = repository.getParticipants(meetingId)
            repository.updateParticipants(
                participants.map { it.copy(amount = 0, isSettled = false) }
            )
            onDone()
        }
    }

    companion object {
        data class SettlementCalc(
            val femalePerPerson: Long,
            val malePerPerson: Long,
            val equalSplit: Boolean
        )

        /**
         * @param femaleAmountEntered 여자 1인 금액 입력란에 값이 있으면 true
         */
        fun computeSettlement(
            settlementAmount: Long,
            femaleAmountInput: Long,
            femaleCount: Int,
            maleCount: Int,
            femaleAmountEntered: Boolean
        ): SettlementCalc {
            val totalCount = maleCount + femaleCount
            if (settlementAmount <= 0 || totalCount <= 0) {
                return SettlementCalc(0, 0, true)
            }
            if (!femaleAmountEntered) {
                val equal = settlementAmount / totalCount
                return SettlementCalc(equal, equal, equalSplit = true)
            }
            val femalePer = femaleAmountInput
            val malePer = computeMaleAmount(settlementAmount, femaleAmountInput, femaleCount, maleCount)
            return SettlementCalc(femalePer, malePer, equalSplit = false)
        }

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
    private val repository: SettlementRepository,
    private val settingsStore: SettingsStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettlementViewModel::class.java)) {
            return SettlementViewModel(repository, settingsStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
