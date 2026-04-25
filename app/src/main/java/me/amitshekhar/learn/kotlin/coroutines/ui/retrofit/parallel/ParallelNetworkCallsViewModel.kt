package me.amitshekhar.learn.kotlin.coroutines.ui.retrofit.parallel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetUsersParallelUseCase
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class ParallelNetworkCallsViewModel(
    getUsersParallelUseCase: GetUsersParallelUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<List<ApiUser>>> = getUsersParallelUseCase()
        .map { result ->
            when (result) {
                is Resource.Success -> UiState.Success(result.data)
                is Resource.Error -> UiState.Error(result.message)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

}
