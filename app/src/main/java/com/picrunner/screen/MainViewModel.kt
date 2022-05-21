package com.picrunner.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _imagesFlow = MutableStateFlow<List<Any>>(listOf())
            val imagesFlow: StateFlow<List<Any>> = _imagesFlow

    private val _isFetchingInProgress = MutableStateFlow(false)
            val isFetchingInProgress: StateFlow<Boolean> = _isFetchingInProgress

    fun startWalk() {
        viewModelScope.launch {
            _isFetchingInProgress.emit(true)
        }
    }

    fun stopWalk() {
        viewModelScope.launch {
            _isFetchingInProgress.emit(false)
        }
    }
}