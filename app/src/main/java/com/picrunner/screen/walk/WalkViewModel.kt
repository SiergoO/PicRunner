package com.picrunner.screen.walk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.picrunner.domain.model.Photo
import com.picrunner.domain.usecase.UseCase
import com.picrunner.domain.usecase.search.GetAllPhotosFromDBUseCaseParam
import com.picrunner.domain.usecase.search.GetAllPhotosFromDBUseCaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalkViewModel @Inject constructor(
    private val getAllPhotosFromDBUseCase: UseCase<GetAllPhotosFromDBUseCaseParam, GetAllPhotosFromDBUseCaseResult>
): ViewModel() {

    private val _savedPhotos = MutableStateFlow<List<Photo>>(listOf())
    val savedPhotos = _savedPhotos.asStateFlow()

    private val _errorChannel = Channel<Throwable>(Channel.UNLIMITED)
    val errorFlow: Flow<Throwable> = _errorChannel.receiveAsFlow()

    suspend fun getSavedPhotos() {
        viewModelScope.launch {
            getAllPhotosFromDBUseCase.execute(GetAllPhotosFromDBUseCaseParam).fold(
                onSuccess = {
                    _savedPhotos.emit(it)
                },
                onFailure = {
                    _errorChannel.send(it)
                }
            )
        }
    }
}