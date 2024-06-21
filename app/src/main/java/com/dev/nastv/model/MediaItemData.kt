package com.smb.app.addsapp.model

import java.io.Serializable

data class MediaItemData(
    val sourceUrl:String,
    val  mediaType:Type,
    val duration:Long,
    var thumbnailImg:String="https://images.pexels.com/photos/461940/pexels-photo-461940.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"
): Serializable
enum class Type{
   Video,Image,Anniversary,Birthday,NewJoinee
}

data class MediaItemData1(
    val mediaType: Type,
    var name:String?="",
    var date:String?="",
    var position:String?="",
    var educationalQualification:String?="",
    var professionalBackground:String?="",
    var message:String?="",
    var yearOfAnniversary:Int=0,
    var hobbies:String?="",
    val sourceUrl:String,
    var thumbnailImg:String="https://images.pexels.com/photos/461940/pexels-photo-461940.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
    var programTitle:String?=""



)
