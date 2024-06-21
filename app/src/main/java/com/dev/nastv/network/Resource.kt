package com.dev.nastv.network

import com.dev.nastv.apis.ErrorType

sealed class Resource<T> {
    class Loading<T> : Resource<T>()

    data class Success<T>(
        val data: T?
    ) : Resource<T>()

    data class Error<T>(
        val errorMessage: String?,
        val errorType: ErrorType
    ) : Resource<T>()
}
