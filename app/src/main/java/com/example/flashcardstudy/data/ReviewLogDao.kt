package com.example.flashcardstudy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reviewLog: ReviewLog): Long

    @Query("SELECT * FROM review_logs ORDER BY reviewedAtMillis DESC")
    fun observeAllReviewLogs(): Flow<List<ReviewLog>>
}