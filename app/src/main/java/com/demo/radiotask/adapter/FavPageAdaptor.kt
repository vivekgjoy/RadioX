package com.demo.radiotask.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.demo.radiotask.R
import com.demo.radiotask.databinding.FavoriteItemBinding
import com.demo.radiotask.model.ChannelsModel

class FavPageAdaptor(val context: Context, private val favList: ArrayList<ChannelsModel>) :
    RecyclerView.Adapter<FavPageAdaptor.ViewHolderClass>() {

    inner class ViewHolderClass(val binding: FavoriteItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val binding =
            FavoriteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderClass(binding)
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {


        val currentItem = favList[position]
        with(holder) {
            binding.channelNum.text = currentItem.channelNum.toString()
            binding.deleteFav.setOnClickListener {

//                binding.tunebarRv.smoothScrollToPosition(position + 1)

                val builder = AlertDialog.Builder(context)
                builder.setTitle("Do You want to delete?")
                builder.setNeutralButton("No", null)
                builder.setPositiveButton("Yes") { _, _ ->
                    favList.removeAt(position)
                    !currentItem.isFavorite
                    notifyDataSetChanged()
                }
                val dialog = builder.create()
                dialog.setCancelable(true)
                dialog.show()

            }
        }
    }


    override fun getItemCount(): Int {
        return favList.size
    }
}