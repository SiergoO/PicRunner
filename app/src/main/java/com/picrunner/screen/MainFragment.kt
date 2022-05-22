package com.picrunner.screen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.picrunner.R
import com.picrunner.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentMainBinding? = null
    val binding: FragmentMainBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater)
        binding.mainViewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launchWhenCreated {
            viewModel.isFetchingInProgress.collect { inProgress ->
                binding.apply {
                    btnStart.visibility = if (inProgress) View.GONE else View.VISIBLE
                    btnStop.visibility = if (inProgress) View.VISIBLE else View.GONE
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.imagesFlow.collect { image ->
                Toast.makeText(context, image.toString(), Toast.LENGTH_LONG).show()
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.errorFlow.collect { error ->
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }
}