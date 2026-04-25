package me.amitshekhar.learn.kotlin.coroutines.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.amitshekhar.learn.kotlin.coroutines.data.local.entity.User
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.repository.UserRepository

class GetUsersFromDbUseCase(private val userRepository: UserRepository) {

    operator fun invoke(): Flow<Resource<List<User>>> {
        return userRepository.getUsersFromDb()
    }

}
