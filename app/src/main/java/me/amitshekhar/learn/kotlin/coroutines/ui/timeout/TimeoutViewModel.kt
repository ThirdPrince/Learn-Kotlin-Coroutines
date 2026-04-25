package me.amitshekhar.learn.kotlin.coroutines.ui.timeout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetUsersUseCase
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class TimeoutViewModel(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ApiUser>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ApiUser>>> = _uiState

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                withTimeout(100) {
                    getUsersUseCase().collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _uiState.value = UiState.Success(result.data)
                            }
                            is Resource.Error -> {
                                _uiState.value = UiState.Error(result.message)
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _uiState.value = UiState.Error("TimeoutCancellationException")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Something Went Wrong")
            }
        }
    }

}
