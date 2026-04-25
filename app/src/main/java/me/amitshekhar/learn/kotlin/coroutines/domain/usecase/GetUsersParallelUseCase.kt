package me.amitshekhar.learn.kotlin.coroutines.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.repository.UserRepository

class GetUsersParallelUseCase(private val userRepository: UserRepository) {

    operator fun invoke(): Flow<Resource<List<ApiUser>>> {
        return combine(
            userRepository.getUsers(),
            userRepository.getMoreUsers()
        ) { usersResult, moreUsersResult ->
            if (usersResult is Resource.Success && moreUsersResult is Resource.Success) {
                val allUsers = mutableListOf<ApiUser>()
                allUsers.addAll(usersResult.data)
                allUsers.addAll(moreUsersResult.data)
                Resource.Success(allUsers)
            } else {
                val errorMessage = when {
                    usersResult is Resource.Error -> usersResult.message
                    moreUsersResult is Resource.Error -> moreUsersResult.message
                    else -> "Something Went Wrong"
                }
                Resource.Error(errorMessage)
            }
        }
    }
}
