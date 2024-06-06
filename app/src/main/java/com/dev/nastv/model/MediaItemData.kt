package com.smb.app.addsapp.model

data class MediaItemData(
    val sourceUrl:String,
    val  mediaType:Type,
    val duration:Long,
    var thumbnailImg:String="https://images.pexels.com/photos/461940/pexels-photo-461940.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"
)
enum class Type{
   Vidio,Image
}
