package com.dev.nastv.apis

import com.dev.nastv.model.MediaResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {


    @GET("/tv_app")
    fun getMediaItems() : Flow<ApiResponse<BaseResponse<MediaResponse>>>         //MediaResponse
}