package me.amitshekhar.learn.kotlin.coroutines.domain.repository

import kotlinx.coroutines.flow.Flow
import me.amitshekhar.learn.kotlin.coroutines.data.local.entity.User
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource

interface UserRepository {

    fun getUsers(): Flow<Resource<List<ApiUser>>>

    fun getMoreUsers(): Flow<Resource<List<ApiUser>>>

    fun getUsersWithError(): Flow<Resource<List<ApiUser>>>

    fun getUsersFromDb(): Flow<Resource<List<User>>>

    suspend fun insertUsersToDb(users: List<User>)

}
