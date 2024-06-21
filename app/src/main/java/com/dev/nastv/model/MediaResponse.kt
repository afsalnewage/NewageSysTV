package com.dev.nastv.model

import com.google.gson.annotations.SerializedName

data class MediaResponse(
//    val count: Int,
//    val limit: Int,
//    val offset: Int,
    //@SerializedName("data")
    val tv_apps: ArrayList<TvMedia>
)