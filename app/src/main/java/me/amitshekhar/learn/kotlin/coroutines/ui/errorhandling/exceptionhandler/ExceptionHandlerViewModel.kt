package me.amitshekhar.learn.kotlin.coroutines.ui.errorhandling.exceptionhandler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetUsersUseCase
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class ExceptionHandlerViewModel(
    getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    // 直接通过 Flow 变换定义 uiState
    val uiState: StateFlow<UiState<List<ApiUser>>> = getUsersUseCase()
        .map { result ->
            when (result) {
                is Resource.Success -> UiState.Success(result.data)
                is Resource.Error -> UiState.Error(result.message)
            }
        }
        .onStart { emit(UiState.Loading) }
        .catch { emit(UiState.Error("Something Went Wrong")) } // 替代原有的 CoroutineExceptionHandler
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

}
