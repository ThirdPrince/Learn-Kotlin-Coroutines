package me.amitshekhar.learn.kotlin.coroutines.ui.task.onetask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class LongRunningTaskViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val uiState: StateFlow<UiState<String>> = _uiState

    init {
        startLongRunningTask()
    }

    private fun startLongRunningTask() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // do a long running task
                doLongRunningTask()
                _uiState.value = UiState.Success("Task Completed")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Something Went Wrong")
            }
        }
    }

    private suspend fun doLongRunningTask() {
        withContext(Dispatchers.Default) {
            // your code for doing a long running task
            // Added delay to simulate
            delay(5000)
        }
    }

}
