package com.dev.nastv.apis

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/** Created by Noushad on 21-08-2023.
 * Copyright (c) 2023 NewAgeSys. All rights reserved.
 */

/**
 * Base Gson class structure of all api responses.
 */
data class BaseResponse<T>(

//        @field:SerializedName("function")
//        val function: String,


//        @field:SerializedName("status")
//        val status: Boolean,

//        @field:SerializedName("status_code")
//        val statusCode: Int,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("data")
    var data: T?,
    @field:SerializedName("timestamp")
    val timestamp: String

)