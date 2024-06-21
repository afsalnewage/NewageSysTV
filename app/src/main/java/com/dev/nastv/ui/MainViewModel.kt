package com.dev.nastv.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.nastv.apis.ApiEmptyResponse
import com.dev.nastv.apis.ApiErrorResponse
import com.dev.nastv.apis.ApiSuccessResponse
import com.dev.nastv.apis.BaseResponse
import com.dev.nastv.apis.ErrorType
import com.dev.nastv.model.MediaResponse
import com.dev.nastv.network.Resource
import com.dev.nastv.repository.AppRepository
import com.dev.nastv.uttils.mutableEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    private val _mediaDataState = mutableEventFlow<Resource<BaseResponse<MediaResponse>>>()

    val mediaDataState get() = _mediaDataState.asSharedFlow()

    init {

        getMedeaItems()
    }

    fun getMedeaItems() {
        viewModelScope.launch {

            repository.getMediaItems().onStart {
                _mediaDataState.tryEmit(Resource.Loading())
            }.collectLatest {


                when (it) {
                    is ApiEmptyResponse -> {

                    }

                    is ApiErrorResponse -> {
                        _mediaDataState.tryEmit(Resource.Error(it.errorMessage.toString(), errorType = it.errorType))
                    }

                    is ApiSuccessResponse -> {

                        if (it.data != null) {
                            _mediaDataState.tryEmit(Resource.Success(it.data))
                        } else {
                            _mediaDataState.tryEmit(
                                Resource.Error(
                                    it.data?.message,
                                    ErrorType.OtherError
                                )
                            )
                        }
                    }

                    else -> {}
                }
            }


        }
    }


}