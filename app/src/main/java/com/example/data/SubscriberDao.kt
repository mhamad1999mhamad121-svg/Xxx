package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriberDao {
    @Query("SELECT * FROM subscribers ORDER BY createdAt DESC")
    fun getAllSubscribers(): Flow<List<Subscriber>>

    @Query("SELECT * FROM subscribers WHERE id = :id LIMIT 1")
    fun getSubscriberById(id: Int): Flow<Subscriber?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriber(subscriber: Subscriber): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscribers(subscribers: List<Subscriber>): List<Long>

    @Update
    suspend fun updateSubscriber(subscriber: Subscriber)

    @Query("DELETE FROM subscribers WHERE id = :id")
    suspend fun deleteSubscriberById(id: Int)
}
