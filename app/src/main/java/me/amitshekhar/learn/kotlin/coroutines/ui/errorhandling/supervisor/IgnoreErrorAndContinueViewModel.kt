package me.amitshekhar.learn.kotlin.coroutines.ui.errorhandling.supervisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetMoreUsersUseCase
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetUsersWithErrorUseCase
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class IgnoreErrorAndContinueViewModel(
    getUsersWithErrorUseCase: GetUsersWithErrorUseCase,
    getMoreUsersUseCase: GetMoreUsersUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<List<ApiUser>>> = combine(
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

        // 这里的强制类型转换是为了让编译器推导出 Flow 的类型为 UiState 而不是具体的 Success
        UiState.Success(allUsersFromApi) as UiState<List<ApiUser>>
    }.catch { e ->
        emit(UiState.Error(e.message ?: "Something Went Wrong"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

}
