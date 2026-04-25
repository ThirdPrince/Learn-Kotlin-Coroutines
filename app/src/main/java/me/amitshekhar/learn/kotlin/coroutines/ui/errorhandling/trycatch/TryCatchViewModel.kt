package me.amitshekhar.learn.kotlin.coroutines.ui.errorhandling.trycatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetUsersUseCase
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class TryCatchViewModel(
    getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<List<ApiUser>>> = getUsersUseCase()
        .map { result ->
            when (result) {
                is Resource.Success -> UiState.Success(result.data)
                is Resource.Error -> UiState.Error(result.message)
            }
        }
        .catch { e -> emit(UiState.Error(e.message ?: "Something Went Wrong")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

}
