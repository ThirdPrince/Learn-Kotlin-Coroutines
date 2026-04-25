package me.amitshekhar.learn.kotlin.coroutines.ui.timeout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetUsersUseCase
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class TimeoutViewModel(
    getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<List<ApiUser>>> = flow {
        withTimeout(100) {
            getUsersUseCase().collect { result ->
                emit(when (result) {
                    is Resource.Success -> UiState.Success(result.data)
                    is Resource.Error -> UiState.Error(result.message)
                })
            }
        }
    }.catch { e ->
        when (e) {
            is TimeoutCancellationException -> emit(UiState.Error("TimeoutCancellationException"))
            else -> emit(UiState.Error("Something Went Wrong"))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

}
