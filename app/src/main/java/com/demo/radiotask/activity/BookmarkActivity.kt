package com.demo.radiotask.activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.radiotask.adapter.BookmarkAdapter
import com.demo.radiotask.databinding.ActivityBookmarkItemsBinding
import com.demo.radiotask.model.ChannelsModel

class BookmarkActivity : AppCompatActivity() {

    private val binding by lazy { ActivityBookmarkItemsBinding.inflate(layoutInflater) }
    private var bookList = ArrayList<ChannelsModel>()
    private val bookmarkAdapter by lazy { BookmarkAdapter(this,bookList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bookList.clear()
//        filter channel list with fav
        bookList =
            HomePageActivity.channelDataItems.filter { item -> item.isBooked } as ArrayList<ChannelsModel>

        Log.v("Fav", bookList.toString())
        binding.bookmarkRv.adapter = bookmarkAdapter
        bookmarkAdapter.diffUtil.submitList(bookList)

//        item decoration for lines in recyclerview
        binding.bookmarkRv.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
    }
}