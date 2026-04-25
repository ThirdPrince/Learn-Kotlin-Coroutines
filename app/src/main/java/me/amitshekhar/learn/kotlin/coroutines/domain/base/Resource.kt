package me.amitshekhar.learn.kotlin.coroutines.domain.base

sealed interface Resource<out T> {

    data class Success<T>(val data: T) : Resource<T>

    data class Error(val message: String) : Resource<Nothing>

}
