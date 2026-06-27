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

    enum class SplitMode { EQUAL, GENDER_DIFF, FEMALE_AMOUNT }

    private val _language = MutableStateFlow(settingsStore.getLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        settingsStore.setLanguage(language)
        _language.value = language
    }

    /** 시스템 언어 자동 선택(사용자 미설정) 시 갱신 */
    fun refreshLanguageFromSystem() {
        if (!settingsStore.hasUserLanguagePreference()) {
            _language.value = settingsStore.getLanguage()
        }
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

    fun deleteMeetings(ids: Collection<Long>) {
        viewModelScope.launch {
            ids.forEach { repository.deleteMeeting(it) }
        }
    }

    fun clearReceiptPhoto(meetingId: Long) {
        viewModelScope.launch {
            repository.getMeeting(meetingId)?.let {
                repository.updateMeeting(it.copy(receiptPhotoUri = null))
            }
        }
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

    /** 남/여 인원수만큼 기본 이름으로 참가자를 DB에 일괄 등록 */
    fun registerParticipantsFromCounts(
        meetingId: Long,
        maleCount: Int,
        femaleCount: Int,
        maleNameForIndex: (Int) -> String,
        femaleNameForIndex: (Int) -> String,
        paymentType: PaymentType = PaymentType.CASH
    ) {
        viewModelScope.launch {
            if (maleCount <= 0 && femaleCount <= 0) return@launch
            val existing = repository.getParticipants(meetingId)
            var slots = MAX_PARTICIPANTS - existing.size
            if (slots <= 0) return@launch

            val existingMale = existing.count { it.gender == Gender.MALE }
            val existingFemale = existing.count { it.gender == Gender.FEMALE }
            val toInsert = mutableListOf<Participant>()

            (1..maleCount).take(slots).forEach { i ->
                toInsert.add(
                    Participant(
                        meetingId = meetingId,
                        name = maleNameForIndex(existingMale + i),
                        gender = Gender.MALE,
                        paymentType = paymentType
                    )
                )
                slots--
            }
            (1..femaleCount).take(slots).forEach { i ->
                toInsert.add(
                    Participant(
                        meetingId = meetingId,
                        name = femaleNameForIndex(existingFemale + i),
                        gender = Gender.FEMALE,
                        paymentType = paymentType
                    )
                )
            }

            repository.insertParticipants(toInsert)
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
     * - 여자 금액 미입력 + 남여차이 미입력: 균등 분배
     * - 여자 금액 미입력 + 남여차이 입력: 남자 = 여자 + 차액 으로 분배
     * - 여자 금액 입력: 여자 1인 금액 적용, 나머지를 남자 인원 수로 분배
     */
    fun completeSettlement(
        meetingId: Long,
        settlementAmount: Long,
        femaleAmountInput: Long,
        genderDiffInput: Long,
        femaleAmountEntered: Boolean,
        genderDiffEntered: Boolean,
        participantsUi: List<Participant>
    ) {
        viewModelScope.launch {
            val meeting = repository.getMeeting(meetingId) ?: return@launch
            val maleCount = participantsUi.count { it.gender == Gender.MALE }
            val femaleCount = participantsUi.count { it.gender == Gender.FEMALE }
            val calc = computeSettlement(
                settlementAmount = settlementAmount,
                femaleAmountInput = femaleAmountInput,
                genderDiffInput = genderDiffInput,
                femaleCount = femaleCount,
                maleCount = maleCount,
                femaleAmountEntered = femaleAmountEntered,
                genderDiffEntered = genderDiffEntered
            )

            repository.updateMeeting(
                meeting.copy(
                    settlementAmount = settlementAmount,
                    femaleAmount = calc.femalePerPerson,
                    maleAmount = calc.malePerPerson,
                    genderDiffAmount = calc.storedGenderDiff,
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
                meeting.copy(
                    settlementAmount = 0,
                    femaleAmount = 0,
                    maleAmount = 0,
                    genderDiffAmount = 0
                )
            )
            val participants = repository.getParticipants(meetingId)
            repository.updateParticipants(
                participants.map { it.copy(amount = 0, isSettled = false) }
            )
            onDone()
        }
    }

    companion object {
        const val MAX_PARTICIPANTS = 30

        data class SettlementCalc(
            val femalePerPerson: Long,
            val malePerPerson: Long,
            val mode: SplitMode,
            /** DB 저장용 남여차이 금액 (여자 금액 입력 모드에서는 0) */
            val storedGenderDiff: Long = 0
        )

        /**
         * @param femaleAmountEntered 여자 1인 금액 입력란에 값이 있으면 true (남여차이 무시)
         * @param genderDiffEntered 남여차이 금액 입력란에 값이 있으면 true
         */
        fun computeSettlement(
            settlementAmount: Long,
            femaleAmountInput: Long,
            genderDiffInput: Long,
            femaleCount: Int,
            maleCount: Int,
            femaleAmountEntered: Boolean,
            genderDiffEntered: Boolean
        ): SettlementCalc {
            val totalCount = maleCount + femaleCount
            if (settlementAmount <= 0 || totalCount <= 0) {
                return SettlementCalc(0, 0, SplitMode.EQUAL)
            }
            if (femaleAmountEntered) {
                val femalePer = femaleAmountInput
                val malePer = computeMaleAmount(settlementAmount, femaleAmountInput, femaleCount, maleCount)
                return SettlementCalc(femalePer, malePer, SplitMode.FEMALE_AMOUNT, storedGenderDiff = 0)
            }
            if (genderDiffEntered && maleCount > 0) {
                val (femalePer, malePer) = computeGenderDiffSplit(
                    settlementAmount, genderDiffInput, femaleCount, maleCount
                )
                return SettlementCalc(
                    femalePer, malePer, SplitMode.GENDER_DIFF, storedGenderDiff = genderDiffInput
                )
            }
            val equal = settlementAmount / totalCount
            return SettlementCalc(equal, equal, SplitMode.EQUAL)
        }

        /**
         * 남여차이 D: 여자 1인 = (총액 − D×남자수) ÷ 총인원, 남자 1인 = 여자 1인 + D
         */
        fun computeGenderDiffSplit(
            settlementAmount: Long,
            genderDiff: Long,
            femaleCount: Int,
            maleCount: Int
        ): Pair<Long, Long> {
            val totalCount = maleCount + femaleCount
            if (totalCount <= 0) return 0L to 0L
            val maleExtraTotal = genderDiff * maleCount
            val remaining = (settlementAmount - maleExtraTotal).coerceAtLeast(0)
            val femalePer = remaining / totalCount
            val malePer = femalePer + genderDiff
            return femalePer to malePer
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
