package com.picrunner.screen

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _imagesFlow = MutableStateFlow<List<Any>>(listOf())
            val imagesFlow: StateFlow<List<Any>> = _imagesFlow

    fun startWalk() {

    }

    fun stopWalk() {

    }
}