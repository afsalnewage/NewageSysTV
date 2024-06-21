package com.dev.nastv.network

import com.dev.nastv.apis.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.awaitResponse
import java.lang.reflect.Type

class FlowCallAdapter(
    private val responseType: Type
) : CallAdapter<Type, Flow<ApiResponse<Type>>> {
    override fun responseType() = responseType

    override fun adapt(call: Call<Type>) = flow {
        val response = call.awaitResponse()
        emit(ApiResponse.create(response))
    }.catch { exception ->
        emit(ApiResponse.create(exception))
    }
}
