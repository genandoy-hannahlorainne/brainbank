package com.example.flashcardstudy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>): List<Long>

    @Query("SELECT * FROM categories ORDER BY name")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories ORDER BY name")
    fun observeAllCategories(): Flow<List<Category>>

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(category: Category)
}