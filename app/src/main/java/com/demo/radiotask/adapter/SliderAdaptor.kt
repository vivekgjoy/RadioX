package com.demo.radiotask.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.demo.radiotask.R
import com.demo.radiotask.model.AddSliderData

class SliderAdapter(private val viewPager : ViewPager2, private val imgList : ArrayList<AddSliderData>):
    RecyclerView.Adapter<SliderAdapter.SliderViewHolder>()
{

    inner class SliderViewHolder (var v: View) : RecyclerView.ViewHolder(v){
        val img = v.findViewById<ImageView>(R.id.imageSlider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.add_slider_item, parent, false)
        return SliderViewHolder(v)
    }
    private val run = object :Runnable {
        override fun run() {
            imgList.addAll(imgList)
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val listImg = imgList[position]
        holder.img.setImageResource(listImg.img)
        if (position == imgList.size -2)
            viewPager.post(run)
    }

    override fun getItemCount(): Int {
        return imgList.size
    }
}