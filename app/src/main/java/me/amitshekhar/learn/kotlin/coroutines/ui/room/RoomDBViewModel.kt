package me.amitshekhar.learn.kotlin.coroutines.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetUsersFromDbUseCase
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class RoomDBViewModel(
    private val getUsersFromDbUseCase: GetUsersFromDbUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ApiUser>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ApiUser>>> = _uiState

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getUsersFromDbUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val apiUsers = result.data.map { user ->
                            ApiUser(
                                user.id,
                                user.name ?: "",
                                user.email ?: "",
                                user.avatar ?: ""
                            )
                        }
                        _uiState.value = UiState.Success(apiUsers)
                    }
                    is Resource.Error -> {
                        _uiState.value = UiState.Error(result.message)
                    }
                }
            }
        }
    }

}
