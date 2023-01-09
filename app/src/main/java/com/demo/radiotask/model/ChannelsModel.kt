package com.demo.radiotask.model

data class ChannelsModel(
    val channelImg: Int = 0,
    val channelName: String = "",
    val albumName: String = "",
    var isHDChannel: Boolean = false,
    var channelNum:Double,
    var audio: Int = 0,
    var isFavorite: Boolean = false,
    var isBooked:Boolean = false,

)