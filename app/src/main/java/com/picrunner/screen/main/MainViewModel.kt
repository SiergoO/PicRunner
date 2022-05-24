package com.picrunner.screen.main

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.picrunner.domain.model.Photo
import com.picrunner.domain.usecase.UseCase
import com.picrunner.domain.usecase.search.SearchPhotosParam
import com.picrunner.domain.usecase.search.SearchPhotosResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val searchPhotosUseCase: UseCase<SearchPhotosParam, SearchPhotosResult>
) : ViewModel() {

    private val _imagesFlow = MutableStateFlow<List<Photo>>(listOf())
    val imagesFlow: StateFlow<List<Photo>> = _imagesFlow

    private val _locationChannel = Channel<Location>(Channel.UNLIMITED)
    val locationFlow: Channel<Location> = _locationChannel

    private val _errorChannel = Channel<Throwable>(Channel.UNLIMITED)
    val errorFlow: Flow<Throwable> = _errorChannel.receiveAsFlow()

    private val _isLocationDetectionProgress = MutableStateFlow(false)
    val isLocationDetectionInProgress: MutableStateFlow<Boolean> = _isLocationDetectionProgress

    fun startWalk() {
        viewModelScope.launch {
            _isLocationDetectionProgress.emit(true)
            _locationChannel.receiveAsFlow().collect {
            val r = searchPhotosUseCase.execute(
                SearchPhotosParam(it.latitude, it.longitude, 0.2)
            )
            r.fold(
                onSuccess = { photos ->
                    _imagesFlow.emit(photos)
                },
                onFailure = { error ->
                    _errorChannel.trySend(error)
                }
            )
            }
        }
    }


    fun stopWalk() {
        viewModelScope.launch {
            _isLocationDetectionProgress.emit(false)
        }
    }
}