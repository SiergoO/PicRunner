package com.picrunner.screen.main

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.picrunner.domain.usecase.UseCase
import com.picrunner.domain.usecase.search.GetNearestPhotoUrlListParam
import com.picrunner.domain.usecase.search.GetNearestPhotoUrlListResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalkViewModel @Inject constructor(
    private val searchPhotosUseCase: UseCase<GetNearestPhotoUrlListParam, GetNearestPhotoUrlListResult>,
) : ViewModel() {

    private var photoUriList = listOf<String>()

    private val _photoUriListFlow = MutableSharedFlow<List<String>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val photoUriListFlow = _photoUriListFlow

    private val _errorChannel = Channel<Throwable>(Channel.UNLIMITED)
    val errorFlow: Flow<Throwable> = _errorChannel.receiveAsFlow()

    fun sendLocation(locationFlow: Flow<List<Location>>) {
        viewModelScope.launch {
            locationFlow
                .collect { locationList ->
                val r = searchPhotosUseCase.execute(
                    GetNearestPhotoUrlListParam(locationList.map { Pair(it.latitude, it.longitude) })
                )
                r.fold(
                    onSuccess = { photoUrlList ->
                        photoUriList += photoUrlList
                        _photoUriListFlow.emit(photoUrlList)
                    },
                    onFailure = { error ->
                        _errorChannel.trySend(error)
                    }
                )
            }
        }
    }
}