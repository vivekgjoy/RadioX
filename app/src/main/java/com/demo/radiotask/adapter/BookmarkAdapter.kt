package com.demo.radiotask.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.demo.radiotask.databinding.BookmarkItemBinding
import com.demo.radiotask.model.ChannelsModel

class BookmarkAdapter(val context: Context, private val bookmarksItems: ArrayList<ChannelsModel>) :
    RecyclerView.Adapter<BookmarkAdapter.ViewHolderClass>() {

    inner class ViewHolderClass(val binding: BookmarkItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val binding =
            BookmarkItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderClass(binding)
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
//        val currentItem = HomePageActivity.bookmarksItems[position]
        val currentItem = diffUtil.currentList[position]
        with(holder) {
            binding.channelImg.setImageResource(currentItem.channelImg)
            binding.channelNum.text = currentItem.channelNum.toString()
            binding.artistName.text = currentItem.albumName

            binding.deleteFav.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Do You want to delete?")
                builder.setNeutralButton("No", null)
                builder.setPositiveButton("Yes") { _, _ ->
                    removeItem(position)
                    currentItem.isBooked = false
                }

                val dialog = builder.create()
                dialog.setCancelable(true)
                dialog.show()
            }
        }
    }

    fun removeItem(position: Int) {
        val currentList = diffUtil.currentList.toMutableList()
        currentList.removeAt(position)
        diffUtil.submitList(currentList)
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