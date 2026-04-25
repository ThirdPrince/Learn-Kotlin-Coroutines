package me.amitshekhar.learn.kotlin.coroutines.data.local

import me.amitshekhar.learn.kotlin.coroutines.data.local.entity.User

interface UserLocalDataSource {
    suspend fun getUsers(): List<User>
    suspend fun insertAll(users: List<User>)
}

class UserLocalDataSourceImpl(private val databaseHelper: DatabaseHelper) : UserLocalDataSource {
    override suspend fun getUsers() = databaseHelper.getUsers()
    override suspend fun insertAll(users: List<User>) = databaseHelper.insertAll(users)
}
