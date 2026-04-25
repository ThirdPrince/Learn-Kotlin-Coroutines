package me.amitshekhar.learn.kotlin.coroutines.ui.task.twotasks

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import me.amitshekhar.learn.kotlin.coroutines.databinding.ActivityLongRunningTaskBinding
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState
import org.koin.androidx.viewmodel.ext.android.viewModel

class TwoLongRunningTasksActivity : AppCompatActivity() {

    private val viewModel: TwoLongRunningTasksViewModel by viewModel()
    private lateinit var binding: ActivityLongRunningTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLongRunningTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupObserver()
        // 由于 ViewModel 使用了 SharedFlow 触发机制，我们需要手动触发任务开始
        viewModel.startLongRunningTask()
    }

    private fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            if (it.data.isNotEmpty()) {
                                binding.textView.text = it.data
                                binding.textView.visibility = View.VISIBLE
                            }
                        }
                        is UiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.textView.visibility = View.GONE
                        }
                        is UiState.Error -> {
                            //Handle Error
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@TwoLongRunningTasksActivity, it.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

}
