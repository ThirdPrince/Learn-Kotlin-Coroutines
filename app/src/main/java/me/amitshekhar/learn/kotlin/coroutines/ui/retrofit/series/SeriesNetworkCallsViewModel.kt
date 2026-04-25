package me.amitshekhar.learn.kotlin.coroutines.ui.retrofit.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetUsersSeriesUseCase
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class SeriesNetworkCallsViewModel(
    getUsersSeriesUseCase: GetUsersSeriesUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<List<ApiUser>>> = getUsersSeriesUseCase()
        .map { result ->
            when (result) {
                is Resource.Success -> UiState.Success(result.data)
                is Resource.Error -> UiState.Error(result.message)
            }
        }
        .onStart { emit(UiState.Loading) }
        .catch { emit(UiState.Error("Something Went Wrong")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

}
