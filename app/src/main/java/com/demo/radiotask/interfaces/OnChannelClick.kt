package com.demo.radiotask.interfaces

import android.icu.text.Transliterator.Position
import com.demo.radiotask.model.ChannelsModel

interface OnChannelClick {
    fun onClick(currentItem : Int)
}