package com.dev.nastv.model

import com.dev.nastv.network.EnumDeserializer
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TvMedia(
    @SerializedName("__v")
    val __v: Int,
    val _id: String,
    val active: Boolean,
    var anniversary_year: Int=0,
    val created_at: String,
    val created_by: Any,
    val deleted_at: Any,
    var educational_qualification:String?="",
    val end_date: String,
    val event_date: String,
    val event_type: String,
//    @SerializedName("event_type")
//    @JsonAdapter(EnumDeserializer::class)
//    val event_type: Type,
    val file_url: String,
    var video_width:Int=0,
    var video_height:Int=0,
    var hobbies: String?="",
    var message: String?="",
    var professional_background: String?="",
    val sort: Int,
    val start_date: String,
    var title: String?="",
    val updated_at: String,
    val updated_by: Any,
    val user_name: String,
    var user_position: String?=""
): Serializable

enum class Type{
    Video,Image,Anniversary,Birthday,NewJoinee,Unknown
}
fun getType(eventType: String): Type {
    return when (eventType) {
        "Video" -> Type.Video
        "Image" -> Type.Image
        "Anniversary" -> Type.Anniversary
        "Birthday" -> Type.Birthday
        "New Joinee" -> Type.NewJoinee

        else -> {Type.Unknown}
    }
}