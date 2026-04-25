package me.amitshekhar.learn.kotlin.coroutines.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.amitshekhar.learn.kotlin.coroutines.data.api.UserRemoteDataSource
import me.amitshekhar.learn.kotlin.coroutines.data.local.UserLocalDataSource
import me.amitshekhar.learn.kotlin.coroutines.data.local.entity.User
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.repository.UserRepository

class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource
) : UserRepository {

    override fun getUsers(): Flow<Resource<List<ApiUser>>> = flow {
        // 1. 首先从本地获取数据
        val localUsers = localDataSource.getUsers()
        if (localUsers.isNotEmpty()) {
            emit(Resource.Success(localUsers.map { it.toApiUser() }))
        }

        // 2. 然后从网络获取最新数据
        try {
            val remoteUsers = remoteDataSource.getUsers()
            // 3. 更新本地数据库缓存
            localDataSource.insertAll(remoteUsers.map { it.toUserEntity() })
            // 4. 发射网络请求后的最新结果
            emit(Resource.Success(remoteUsers))
        } catch (e: Exception) {
            // 如果本地已有数据，则不发射错误，否则发射错误
            if (localUsers.isEmpty()) {
                emit(Resource.Error(e.toString()))
            }
        }
    }

    override fun getMoreUsers(): Flow<Resource<List<ApiUser>>> = flow {
        try {
            emit(Resource.Success(remoteDataSource.getMoreUsers()))
        } catch (e: Exception) {
            emit(Resource.Error(e.toString()))
        }
    }

    override fun getUsersWithError(): Flow<Resource<List<ApiUser>>> = flow {
        try {
            emit(Resource.Success(remoteDataSource.getUsersWithError()))
        } catch (e: Exception) {
            emit(Resource.Error(e.toString()))
        }
    }

    override fun getUsersFromDb(): Flow<Resource<List<User>>> = flow {
        try {
            emit(Resource.Success(localDataSource.getUsers()))
        } catch (e: Exception) {
            emit(Resource.Error(e.toString()))
        }
    }

    override suspend fun insertUsersToDb(users: List<User>) {
        localDataSource.insertAll(users)
    }

    // 简单的转换函数示例，建议实际开发中放入独立的 Mapper
    private fun User.toApiUser() = ApiUser(id, name ?: "", email ?: "", avatar ?: "")
    private fun ApiUser.toUserEntity() = User(id, name, email, avatar)

}
