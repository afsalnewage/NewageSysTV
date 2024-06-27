package com.dev.nastv.apis

import com.dev.nastv.model.MediaResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {


    @GET("/tv_app/events")              //@Query("where") where: String
    fun getMediaItems() : Flow<ApiResponse<BaseResponse<MediaResponse>>>         //MediaResponse
}