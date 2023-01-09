package com.demo.radiotask.activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.radiotask.adapter.FavPageAdaptor
import com.demo.radiotask.databinding.ActivityFavItemsBinding
import com.demo.radiotask.model.ChannelsModel

class FavItemsActivity : AppCompatActivity() {

    private val binding by lazy { ActivityFavItemsBinding.inflate(layoutInflater) }
    private var favList = ArrayList<ChannelsModel>()
    private val favoriteadapter by lazy { FavPageAdaptor(this, favList = favList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        favList.clear()
//        favList.addAll(HomePageActivity.favItems)
//        filter channel list with fav
       favList = HomePageActivity.channelDataItems.filter { item -> item.isFavorite } as ArrayList<ChannelsModel>

        Log.v("Fav", favList.toString())
        binding.favoriteRv.adapter = favoriteadapter

//        item decoration for lines in recyclerview
        binding.favoriteRv.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
    }
}