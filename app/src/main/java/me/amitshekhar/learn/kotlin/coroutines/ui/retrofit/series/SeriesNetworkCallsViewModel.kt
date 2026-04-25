package me.amitshekhar.learn.kotlin.coroutines.ui.retrofit.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.amitshekhar.learn.kotlin.coroutines.data.model.ApiUser
import me.amitshekhar.learn.kotlin.coroutines.domain.base.Resource
import me.amitshekhar.learn.kotlin.coroutines.domain.usecase.GetUsersSeriesUseCase
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class SeriesNetworkCallsViewModel(
    private val getUsersSeriesUseCase: GetUsersSeriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ApiUser>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ApiUser>>> = _uiState

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getUsersSeriesUseCase().collect { result ->
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
    }

}
