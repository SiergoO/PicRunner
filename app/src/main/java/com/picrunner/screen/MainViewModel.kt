package com.picrunner.screen

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

    private val _errorChannel = Channel<Throwable>(Channel.UNLIMITED)
    val errorFlow: Flow<Throwable> = _errorChannel.receiveAsFlow()

    private val _isFetchingInProgress = MutableStateFlow(false)
    val isFetchingInProgress: StateFlow<Boolean> = _isFetchingInProgress

    fun startWalk() {
        viewModelScope.launch {
            val r = searchPhotosUseCase.execute(
                SearchPhotosParam(54.721119, 25.278273, 0.2)
            )
            r.fold(
                onSuccess = {
                    _imagesFlow.emit(it)
                },
                onFailure = {
                    _errorChannel.trySend(it)
                }
            )
            _isFetchingInProgress.emit(true)
        }
    }

    fun stopWalk() {
        viewModelScope.launch {
            _isFetchingInProgress.emit(false)
        }
    }
}