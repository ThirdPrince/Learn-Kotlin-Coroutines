package me.amitshekhar.learn.kotlin.coroutines.data.api

import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser

interface UserRemoteDataSource {
    suspend fun getUsers(): List<ApiUser>
    suspend fun getMoreUsers(): List<ApiUser>
    suspend fun getUsersWithError(): List<ApiUser>
}

class UserRemoteDataSourceImpl(private val apiHelper: ApiHelper) : UserRemoteDataSource {
    override suspend fun getUsers() = apiHelper.getUsers()
    override suspend fun getMoreUsers() = apiHelper.getMoreUsers()
    override suspend fun getUsersWithError() = apiHelper.getUsersWithError()
}
