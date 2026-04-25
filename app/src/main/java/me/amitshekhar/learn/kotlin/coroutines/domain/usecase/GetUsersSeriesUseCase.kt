package me.amitshekhar.learn.kotlin.coroutines.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.repository.UserRepository

class GetUsersSeriesUseCase(private val userRepository: UserRepository) {

    operator fun invoke(): Flow<Resource<List<ApiUser>>> {
        return userRepository.getUsers().flatMapConcat { usersResult ->
            if (usersResult is Resource.Success) {
                userRepository.getMoreUsers().map { moreUsersResult ->
                    if (moreUsersResult is Resource.Success) {
                        val allUsers = mutableListOf<ApiUser>()
                        allUsers.addAll(usersResult.data)
                        allUsers.addAll(moreUsersResult.data)
                        Resource.Success(allUsers)
                    } else {
                        moreUsersResult as Resource.Error
                    }
                }
            } else {
                flow { emit(usersResult as Resource.Error) }
            }
        }
    }
}
