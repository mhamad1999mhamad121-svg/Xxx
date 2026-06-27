package com.example.data

import kotlinx.coroutines.flow.Flow

class SubscriberRepository(private val subscriberDao: SubscriberDao) {
    val allSubscribers: Flow<List<Subscriber>> = subscriberDao.getAllSubscribers()

    fun getSubscriberById(id: Int): Flow<Subscriber?> {
        return subscriberDao.getSubscriberById(id)
    }

    suspend fun insert(subscriber: Subscriber): Long {
        return subscriberDao.insertSubscriber(subscriber)
    }

    suspend fun insertAll(subscribers: List<Subscriber>): List<Long> {
        return subscriberDao.insertSubscribers(subscribers)
    }

    suspend fun update(subscriber: Subscriber) {
        subscriberDao.updateSubscriber(subscriber)
    }

    suspend fun deleteById(id: Int) {
        subscriberDao.deleteSubscriberById(id)
    }
}
