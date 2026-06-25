package com.example.settlementapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meeting: Meeting): Long

    @Update
    suspend fun update(meeting: Meeting)

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM meetings ORDER BY meetingDate DESC, id DESC")
    fun observeAll(): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE id = :id")
    suspend fun getById(id: Long): Meeting?

    @Query("SELECT * FROM meetings WHERE id = :id")
    fun observeById(id: Long): Flow<Meeting?>

    @Transaction
    @Query("SELECT * FROM meetings WHERE id = :id")
    fun observeWithParticipants(id: Long): Flow<MeetingWithParticipants?>

    @Query(
        """
        SELECT substr(meetingDate, 1, 7) AS month,
               COUNT(*) AS meetingCount,
               IFNULL(SUM(settlementAmount), 0) AS totalAmount,
               IFNULL(SUM(totalCount), 0) AS totalParticipants
        FROM meetings
        GROUP BY substr(meetingDate, 1, 7)
        ORDER BY month DESC
        """
    )
    fun observeMonthlySummary(): Flow<List<MonthlySummary>>
}
