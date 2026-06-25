package com.example.settlementapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(participant: Participant): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(participants: List<Participant>)

    @Update
    suspend fun update(participant: Participant)

    @Update
    suspend fun updateAll(participants: List<Participant>)

    @Delete
    suspend fun delete(participant: Participant)

    @Query("SELECT * FROM participants WHERE meetingId = :meetingId ORDER BY id ASC")
    fun observeByMeeting(meetingId: Long): Flow<List<Participant>>

    @Query("SELECT * FROM participants WHERE meetingId = :meetingId ORDER BY id ASC")
    suspend fun getByMeeting(meetingId: Long): List<Participant>

    @Query("SELECT COUNT(*) FROM participants WHERE meetingId = :meetingId")
    suspend fun countByMeeting(meetingId: Long): Int
}
