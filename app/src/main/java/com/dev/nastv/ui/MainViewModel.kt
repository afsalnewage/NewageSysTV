package com.dev.nastv.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.dev.nastv.uttils.AppUittils.getCurrentDate
import com.dev.nastv.uttils.ConnectivityObserver
import com.dev.nastv.uttils.mutableEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AppRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {
    private val _mediaDataState = mutableEventFlow<Resource<BaseResponse<MediaResponse>>>()


    private val _workId = MutableLiveData<UUID?>(null)
    val workId: LiveData<UUID?> get() = _workId
    fun updateWorkId(newWorkId: UUID?) {
        _workId.value = newWorkId
    }

    val mediaDataState get() = _mediaDataState.asSharedFlow()
    private val _retryRequired = MutableStateFlow(false)



//    init {
//        observeConnectivity()
//        getMediaItems()
//    }

//    private fun observeConnectivity() {
//        viewModelScope.launch {
//            connectivityObserver.observe().collect { status ->
//                when (status) {
//                    ConnectivityObserver.Status.Available -> {
//                        if (_retryRequired.value) {
//                            getMediaItems()
//                        }
//                    }
//                    else -> { /* No-op for other statuses */ }
//                }
//            }
//        }
//    }


    @RequiresApi(Build.VERSION_CODES.O)
    private val date = getCurrentDate()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMediaItems() {


        val filterObject = JSONObject()

        val startDateObject = JSONObject()
        startDateObject.put("\$lte", date)
        filterObject.put("start_date", startDateObject)

        val endDateObject = JSONObject()
        endDateObject.put("\$gte", date)
        filterObject.put("end_date", endDateObject)


        viewModelScope.launch {

            repository.getMediaItems(filterObject.toString()).onStart {
                _mediaDataState.tryEmit(Resource.Loading())
            }.collectLatest {


                when (it) {
                    is ApiEmptyResponse -> {

                    }

                    is ApiErrorResponse -> {
                        _mediaDataState.tryEmit(
                            Resource.Error(
                                it.errorMessage.toString(),
                                errorType = it.errorType
                            )
                        )
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



//    fun downloadFile(url: String, outputFile: File, onProgress: (progress: Int) -> Unit, onComplete: (success: Boolean) -> Unit) {
//        viewModelScope.launch {
//            val download = DownloadMedia(url = url, fileName = outputFile.name, status = "Downloading")
//            repository.insertDownload(download)
//
//            val success = downloadFile(url, outputFile, onProgress)
//            val status = if (success) "Completed" else "Failed"
//            repository.updateDownload(download.copy(status = status))
//
//            onComplete(success)
//        }
//    }




}