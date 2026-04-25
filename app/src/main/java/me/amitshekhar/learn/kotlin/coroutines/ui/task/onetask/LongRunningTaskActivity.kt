package me.amitshekhar.learn.kotlin.coroutines.ui.task.onetask

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import me.amitshekhar.learn.kotlin.coroutines.databinding.ActivityLongRunningTaskBinding
import me.amitshekhar.learn.kotlin.coroutines.ui.base.UiState
import org.koin.androidx.viewmodel.ext.android.viewModel

class LongRunningTaskActivity : AppCompatActivity() {

    private val viewModel: LongRunningTaskViewModel by viewModel()
    private lateinit var binding: ActivityLongRunningTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLongRunningTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupObserver()
    }

    private fun setupObserver() {
        viewModel.getUiState().observe(this) {
            when (it) {
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textView.text = it.data
                    binding.textView.visibility = View.VISIBLE
                }
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.textView.visibility = View.GONE
                }
                is UiState.Error -> {
                    //Handle Error
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
