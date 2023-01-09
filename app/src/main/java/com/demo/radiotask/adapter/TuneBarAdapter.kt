package com.demo.radiotask.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.demo.radiotask.R

class TuneBarAdapter : RecyclerView.Adapter<TuneBarAdapter.ViewHolderClass>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolderClass(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = diffUtil.currentList[position]

        if (currentItem % 5.0 == 0.0) {
            holder.rv1Text.text = currentItem.toString()
            holder.rv1Text.visibility = View.VISIBLE
        } else {
//              holder.rv1Text.text = currentItem.toString()
            holder.rv1Text.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return diffUtil.currentList.size
    }

    override fun getItemId(position: Int): Long {
        Log.i("RV_POS", position.toString())
        return super.getItemId(position)
    }

    inner class ViewHolderClass(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //        val rv1Image : ImageView = itemView.findViewById(R.id.artist_image)
        val rv1Text: TextView = itemView.findViewById(R.id.artist_name)
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Double>() {
        override fun areItemsTheSame(
            oldItem: Double,
            newItem: Double
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Double,
            newItem: Double
        ) = oldItem == newItem
    }

    val diffUtil = AsyncListDiffer(this, diffCallback)

}