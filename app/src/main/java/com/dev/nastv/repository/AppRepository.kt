package com.dev.nastv.repository
import com.dev.nastv.apis.ApiResponse
import com.dev.nastv.apis.ApiService
import com.dev.nastv.apis.BaseResponse
import com.dev.nastv.model.MediaResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor( private val apiService: ApiService) {


    fun getMediaItems(where:String):Flow<ApiResponse<BaseResponse<MediaResponse>>>{
        return  apiService.getMediaItems()
    }
}