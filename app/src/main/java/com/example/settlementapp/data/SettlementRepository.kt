package com.example.settlementapp.data

import kotlinx.coroutines.flow.Flow

class SettlementRepository(
    private val meetingDao: MeetingDao,
    private val participantDao: ParticipantDao
) {
    // ---- Meeting ----
    fun observeMeetings(): Flow<List<Meeting>> = meetingDao.observeAll()
    fun observeMeeting(id: Long): Flow<Meeting?> = meetingDao.observeById(id)
    fun observeMeetingWithParticipants(id: Long): Flow<MeetingWithParticipants?> =
        meetingDao.observeWithParticipants(id)
    fun observeMonthlySummary(): Flow<List<MonthlySummary>> = meetingDao.observeMonthlySummary()

    suspend fun getMeeting(id: Long): Meeting? = meetingDao.getById(id)
    suspend fun insertMeeting(meeting: Meeting): Long = meetingDao.insert(meeting)
    suspend fun updateMeeting(meeting: Meeting) = meetingDao.update(meeting)
    suspend fun deleteMeeting(id: Long) = meetingDao.deleteById(id)

    // ---- Participant ----
    fun observeParticipants(meetingId: Long): Flow<List<Participant>> =
        participantDao.observeByMeeting(meetingId)

    suspend fun getParticipants(meetingId: Long): List<Participant> =
        participantDao.getByMeeting(meetingId)

    suspend fun insertParticipant(participant: Participant): Long =
        participantDao.insert(participant)

    suspend fun updateParticipant(participant: Participant) =
        participantDao.update(participant)

    suspend fun updateParticipants(participants: List<Participant>) =
        participantDao.updateAll(participants)

    suspend fun deleteParticipant(participant: Participant) =
        participantDao.delete(participant)

    suspend fun countParticipants(meetingId: Long): Int =
        participantDao.countByMeeting(meetingId)
}
