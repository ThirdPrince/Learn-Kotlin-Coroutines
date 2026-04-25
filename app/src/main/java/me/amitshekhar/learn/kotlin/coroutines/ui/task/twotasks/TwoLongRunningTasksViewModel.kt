package me.amitshekhar.learn.kotlin.coroutines.ui.task.twotasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState

class TwoLongRunningTasksViewModel : ViewModel() {

    private val _startTaskTrigger = MutableSharedFlow<Unit>(replay = 0)

    val uiState: StateFlow<UiState<String>> = _startTaskTrigger
        .flatMapLatest {
            flow {
                emit(UiState.Loading)
                try {
                    val result = combineTasks()
                    emit(UiState.Success(result))
                } catch (e: Exception) {
                    emit(UiState.Error(e.message ?: "Something Went Wrong"))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Success("") // 初始状态为空白或成功
        )

    fun startLongRunningTask() {
        viewModelScope.launch {
            _startTaskTrigger.emit(Unit)
        }
    }

    private suspend fun combineTasks(): String = coroutineScope {
        val resultOneDeferred = async { doLongRunningTaskOne() }
        val resultTwoDeferred = async { doLongRunningTaskTwo() }
        val combinedResult = resultOneDeferred.await() + resultTwoDeferred.await()
        "Task Completed : $combinedResult"
    }

    private suspend fun doLongRunningTaskOne(): Int {
        return withContext(Dispatchers.Default) {
            delay(2000)
            10
        }
    }

    private suspend fun doLongRunningTaskTwo(): Int {
        return withContext(Dispatchers.Default) {
            delay(2000)
            10
        }
    }
}
