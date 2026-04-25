package me.amitshekhar.learn.kotlin.coroutines.ui.errorhandling.supervisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetMoreUsersUseCase
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetUsersWithErrorUseCase
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class IgnoreErrorAndContinueViewModel(
    private val getUsersWithErrorUseCase: GetUsersWithErrorUseCase,
    private val getMoreUsersUseCase: GetMoreUsersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ApiUser>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ApiUser>>> = _uiState

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            combine(
                getUsersWithErrorUseCase(),
                getMoreUsersUseCase()
            ) { usersResult, moreUsersResult ->
                val allUsersFromApi = mutableListOf<ApiUser>()

                if (usersResult is Resource.Success) {
                    allUsersFromApi.addAll(usersResult.data)
                }

                if (moreUsersResult is Resource.Success) {
                    allUsersFromApi.addAll(moreUsersResult.data)
                }

                UiState.Success(allUsersFromApi)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

}
