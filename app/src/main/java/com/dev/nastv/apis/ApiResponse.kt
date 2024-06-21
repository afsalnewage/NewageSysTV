package com.dev.nastv.apis

import android.util.Log
import com.dev.nastv.BuildConfig

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

sealed class ApiResponse<T> {
    companion object {
        fun <T> create(throwable: Throwable): ApiErrorResponse<T> {
            var errorType:ErrorType
            var message: String
            if (BuildConfig.DEBUG) {
             message=   throwable.cause?.message ?: throwable.message!!

                    errorType = when (throwable) {
                        is SocketTimeoutException -> {
                            ErrorType.ServerError
                        }
                        is ConnectException -> {
                            ErrorType.ConnectionError
                        }
                        else -> {
                            ErrorType.ConnectionError
                        }
                    }
            } else {

                when (throwable) {
                    is SocketTimeoutException -> {
                     message=   "Server not responding. Try again in a few"
                        errorType=ErrorType.ServerError
                    }
                    is ConnectException -> {
                     message=    "Sorry, we couldn't connect to internet"
                        errorType=ErrorType.ConnectionError
                    }
                    else -> {
                     message=   "Network connection error"
                        errorType=ErrorType.ConnectionError
                    }
                }
            }
            return ApiErrorResponse(message,errorType)
        }

        fun <T> create(response: Response<T>): ApiResponse<T> {
            return if (response.isSuccessful) {
                val body = response.body()

                if (body == null || response.code() == 204 ) {
                    ApiEmptyResponse()
                } else {
                    ApiSuccessResponse(
                        data = body
                    )
                }
            } else {
                Log.e("ResponseCode",response.code().toString())
                val msg = response.errorBody()?.string()
                val errMsg = if (msg.isNullOrBlank()) {
                    "Error in connection. Please try again."
                } else {
                    Json.parseToJsonElement(msg).jsonObject["message"].toString()
                        .replace("\"", "")
                }
                val errorMsg = if (msg.isNullOrEmpty()) {
                    response.message()
                } else {
                    msg
                }
                ApiErrorResponse(errMsg,ErrorType.ConnectionError)
            }
        }
    }
}

class ApiEmptyResponse<T> : ApiResponse<T>()

data class ApiSuccessResponse<T>(
    val data: T?
) : ApiResponse<T>()

data class ApiErrorResponse<T>(
    val errorMessage: String?,
    val errorType:ErrorType
) : ApiResponse<T>()

enum class ErrorType{
    ServerError,ConnectionError,OtherError
}