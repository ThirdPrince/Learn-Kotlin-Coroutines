package me.amitshekhar.learn.kotlin.coroutines.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetUsersFromDbUseCase
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class RoomDBViewModel(
    getUsersFromDbUseCase: GetUsersFromDbUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<List<ApiUser>>> = getUsersFromDbUseCase()
        .map { result ->
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
                    UiState.Success(apiUsers)
                }
                is Resource.Error -> UiState.Error(result.message)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

}
