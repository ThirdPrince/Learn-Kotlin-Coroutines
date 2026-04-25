package me.amitshekhar.learn.kotlin.coroutines.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.repository.UserRepository

class GetMoreUsersUseCase(private val userRepository: UserRepository) {

    operator fun invoke(): Flow<Resource<List<ApiUser>>> {
        return userRepository.getMoreUsers()
    }

}
