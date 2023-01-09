package com.demo.radiotask.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.demo.radiotask.R
import com.demo.radiotask.interfaces.OnChannelClick

class HdChannelAdaptor(private val callBack: OnChannelClick):
    RecyclerView.Adapter<HdChannelAdaptor.ViewHolderClass>() {

    class ViewHolderClass(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val channelImage : ImageView = itemView.findViewById(R.id.hdchannel1)
        val channelNo : TextView = itemView.findViewById(R.id.channelNumber)
        val ArtistName : TextView = itemView.findViewById(R.id.artistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.hdchannelslist_layout, parent, false)
        return ViewHolderClass(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = diffUtil.currentList[position]
        holder.channelImage.setImageResource(currentItem.channelImg)
        holder.channelNo.text = currentItem.channelName
        holder.ArtistName.text = currentItem.albumName
        holder.itemView.setOnClickListener {
            callBack.onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return diffUtil.currentList.size
    }

    private val diffCallback = object : DiffUtil.ItemCallback<ChannelsModel>() {
        override fun areItemsTheSame(
            oldItem: ChannelsModel,
            newItem: ChannelsModel
        ): Boolean {
            return oldItem.channelNum == newItem.channelNum
        }

        override fun areContentsTheSame(
            oldItem: ChannelsModel,
            newItem: ChannelsModel
        ) = oldItem == newItem
    }

    val diffUtil = AsyncListDiffer(this, diffCallback)
}