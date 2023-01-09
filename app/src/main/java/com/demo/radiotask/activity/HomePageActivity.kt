package com.demo.radiotask.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.demo.radiotask.R
import com.demo.radiotask.adapter.SliderAdapter
import com.demo.radiotask.adapter.TuneBarAdapter
import com.demo.radiotask.databinding.ActivityHomePageBinding
import com.demo.radiotask.interfaces.OnChannelClick
import com.demo.radiotask.model.AddSliderData
import com.demo.radiotask.model.ChannelsModel
import com.demo.radiotask.model.HdChannelAdaptor
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException
import java.util.*
import kotlin.math.abs

@Suppress("NAME_SHADOWING", "DEPRECATION")
class HomePageActivity : AppCompatActivity() {

    companion object {
//        var favItems = ArrayList<ChannelsModel>()
        var channelDataItems = ArrayList<ChannelsModel>()
        var hdChannelDataItems = ArrayList<ChannelsModel>()
        var tuneList = ArrayList<Double>()
    }

    //    lateinit var mediaPlayer: MediaPlayer
    private val binding by lazy { ActivityHomePageBinding.inflate(layoutInflater) }
    private val tuneBarAdapter by lazy { TuneBarAdapter() }
    private val hdChannelAdaptor by lazy {
        HdChannelAdaptor(object : OnChannelClick {
            override fun onClick(currentItem: Int) {
                screenChange(currentItem)
            }
        })
    }

    private lateinit var sliderItemList: ArrayList<AddSliderData>
    private lateinit var sliderAdapter: SliderAdapter
    private lateinit var sliderHandle: Handler
    private lateinit var sliderRun: Runnable
    private lateinit var bottomNavigationView: BottomNavigationView
    private var mediaPlayer = MediaPlayer()

    var currentItem = 0
    var itemPositionForScroll = 0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Glide.with(this).load(R.raw.d_wave).into(binding.screenView)

        // add tunebar stations
        tuneList.clear()
        var i = 45.0
        while (i <= 170.0) {
//            val roundof: Double = String.format("%.3f", i).toDouble()
            tuneList.add(i)
            i += 1.0
        }

        addDataListToRv()
        addHDDataListToRv()
        setAdapter()

        // For Scroll TextBottomNavigationView
        binding.scrollText.setSingleLine()
        binding.scrollText.isSelected = true
        binding.scrollText.text = getString(R.string.scroll_text)

        // For Bottom navigation
        bottomNavigationView = binding.bottomNavigation
//        val badge=bottomNavigationView.getOrCreateBadge(R.id.page1)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page1 -> {
                    val intent = Intent(Intent.ACTION_DIAL)
                    val mobileNumber = "0123456789"
                    intent.data = Uri.parse("tel:$mobileNumber")
                    startActivity(intent)
                    true
                }
                R.id.page2 -> {
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.putExtra(Intent.EXTRA_STREAM, "ali")
                    shareIntent.type = "text/plain"
                    startActivity(
                        Intent.createChooser(
                            shareIntent, resources.getText(R.string.send_to)
                        )
                    )
                    true
                }

                R.id.page3 -> {
                    val url = "https://hdradio.com/"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                    true
                }

                R.id.page4 -> {
                    val intent = Intent(this, FavItemsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.page5 -> {
                    val intent = Intent(this, BookmarkActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    false
                }
            }
        }
        setListeners()
        sliderDatas()
        itemSliderView()
    }

    private fun setAdapter() {
//      channel list adapter
        binding.hdChannelRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.hdChannelRv.adapter = hdChannelAdaptor
        binding.hdChannelRv.setHasFixedSize(true)
        hdChannelAdaptor.diffUtil.submitList(hdChannelDataItems)
    }

    private fun sliderDatas() {
        sliderItemList = ArrayList()
        sliderAdapter = SliderAdapter(binding.viewPagerImageSlider, sliderItemList)
        binding.viewPagerImageSlider.adapter = sliderAdapter
        binding.viewPagerImageSlider.clipToPadding = false
        binding.viewPagerImageSlider.clipChildren = false
        binding.viewPagerImageSlider.offscreenPageLimit = 3
        binding.viewPagerImageSlider.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val comPos = CompositePageTransformer()
        comPos.addTransformer(MarginPageTransformer(40))
        comPos.addTransformer { page, position ->
            val r: Float = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }

        binding.viewPagerImageSlider.setPageTransformer(comPos)
        sliderHandle = Handler()
        sliderRun = Runnable {
            binding.viewPagerImageSlider.currentItem = binding.viewPagerImageSlider.currentItem + 1
        }
        binding.viewPagerImageSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandle.removeCallbacks(sliderRun)
                sliderHandle.postDelayed(sliderRun, 2000)
            }
        })
    }


    private fun startPlaying(item: Int) {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.release()

                mediaPlayer = MediaPlayer.create(this, item)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            } else {
//                Initial condition
                mediaPlayer = MediaPlayer.create(this, item)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }

        } catch (e: IOException) {
            Log.e("EXCEPTION", "prepare() failed")
        }

    }


    override fun onPause() {
        super.onPause()
        sliderHandle.removeCallbacks(sliderRun)
    }

    override fun onResume() {
        super.onResume()
        sliderHandle.postDelayed(sliderRun, 2000)
    }

    private fun itemSliderView() {
        sliderItemList.addAll(listOf(AddSliderData(R.drawable.add1)))
        sliderItemList.addAll(listOf(AddSliderData(R.drawable.add2)))
        sliderItemList.addAll(listOf(AddSliderData(R.drawable.add3)))
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setListeners() {
        binding.radioToolbar.alertImage.setOnClickListener {
            binding.scrollText.isVisible = !binding.scrollText.isVisible
            binding.scrollText.postDelayed(
                { binding.scrollText.visibility = View.GONE }, 5000
            )

        }

        binding.dropIv.setOnClickListener {
            if (binding.hdChannelRv.visibility == View.VISIBLE) {
                binding.hdChannelRv.visibility = View.GONE
                binding.screenViewLayout.visibility = View.VISIBLE
                binding.channelHeadline.visibility = View.VISIBLE
                binding.dropIv.setImageResource(R.drawable.ic_baseline_down_arrow)
            } else {
                binding.screenViewLayout.visibility = View.GONE
                binding.channelHeadline.visibility = View.GONE
                binding.hdChannelRv.visibility = View.VISIBLE
                binding.dropIv.setImageResource(R.drawable.ic_baseline_up_arrow)
            }
        }

//        set tune bar adapter
        val linearLayout = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.tunebarRv.layoutManager = linearLayout
        binding.tunebarRv.adapter = tuneBarAdapter
        binding.tunebarRv.setHasFixedSize(true)
        tuneBarAdapter.diffUtil.submitList(tuneList)

        binding.tunebarRv.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentItem = linearLayout.findFirstVisibleItemPosition()
                itemPositionForScroll = linearLayout.findFirstVisibleItemPosition() // + 1
//                Log.v("POS", currentItem.toString())
                if (currentItem != -1) {
                    updateUI()
                } else {
                    Glide.with(applicationContext).load(R.raw.d_wave).into(binding.screenView)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.v("RV", newState.toString())
            }
        })

        binding.leftMoveIv.setOnClickListener {
            val yCurrentItem = linearLayout.findFirstVisibleItemPosition()
            Log.v("POS", yCurrentItem.toString())
            if (yCurrentItem != -1) {
                Log.v("POS", yCurrentItem.toString())
                binding.tunebarRv.scrollToPosition(yCurrentItem - 1)
            }
        }

        binding.rightMoveIv.setOnClickListener {
            var mCurrentItem = linearLayout.findFirstVisibleItemPosition()
            Log.v("POS", mCurrentItem.toString())
            if (mCurrentItem != -1) {
                mCurrentItem += 11
                Log.v("POS", mCurrentItem.toString())
                binding.tunebarRv.scrollToPosition(mCurrentItem)
            }
        }

        binding.favoriteLayout.setOnClickListener {
            val item = channelDataItems[currentItem]
            Log.v("Fav", item.isFavorite.toString())
            if (currentItem != -1 && !item.isFavorite) {
                item.isFavorite = true
                binding.favoriteBtn.background =
                    resources.getDrawable(R.drawable.ic_baseline_thumb_up_fill)
                Log.v("Fav_add", item.toString())
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Do You want to un-favorite this channel?")
                builder.setNeutralButton("No", null)
                builder.setPositiveButton("Yes") { _, _ ->

                    item.isFavorite = false
                    binding.favoriteBtn.background =
                        resources.getDrawable(R.drawable.ic_baseline_thumb_up)
                }
                val dialog = builder.create()
                dialog.setCancelable(true)
                dialog.show()
                Log.v("Fav_remove", item.toString())
            }
        }

        binding.bookmarkLayout.setOnClickListener {
            val item = channelDataItems[currentItem]
            if (currentItem != -1 && !item.isBooked) {
//                favItems.add(item)
                item.isBooked = true
                binding.bookmarkBtn.background =
                    resources.getDrawable(R.drawable.ic_baseline_bookmark_fill)
                Log.v("Fav", item.toString())
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Do You want to remove Bookmark this channel?")
                builder.setNeutralButton("No", null)
                builder.setPositiveButton("Yes") { _, _ ->

                    item.isBooked = false
                    binding.bookmarkBtn.background =
                        resources.getDrawable(R.drawable.ic_baseline_bookmark_border)
                }
                val dialog = builder.create()
                dialog.setCancelable(true)
                dialog.show()
                Log.v("Fav_remove", item.toString())
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {

//            show frequency
        val item = channelDataItems[itemPositionForScroll]
        val ex = tuneList[itemPositionForScroll + 5]

        if (item.isFavorite == true) {
            binding.favoriteBtn.background =
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_thumb_up_fill)
        } else if (item.isFavorite == false) {
            binding.favoriteBtn.background =
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_thumb_up)
        }

        if (item.isBooked) {
            binding.bookmarkBtn.background =
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_bookmark_fill)
        } else {
            binding.bookmarkBtn.background =
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_bookmark_border)
        }

        // 8761132343
        if (ex % 5.0 != 0.0) {
            val item = channelDataItems[itemPositionForScroll]
//            odd numbers
            Glide.with(this).load(R.raw.d_wave).into(binding.screenView)

//            audio
            startPlaying(item.audio)

            binding.frequencyName.text = ex.toString() + " Mhz"
            binding.channelHeadline.text = item.albumName
            binding.radioToolbar.alertImage.visibility = View.GONE
            binding.radioToolbar.hdIcon.visibility = View.GONE
            binding.dropIv.visibility = View.GONE
            binding.scrollText.visibility = View.GONE
            binding.hdChannelRv.visibility = View.GONE
            binding.screenViewLayout.visibility = View.VISIBLE
        } else {
            binding.frequencyName.text = "$ex Mhz"
            if (item.channelNum.toString() == "100.0" || item.isHDChannel) {
                addHDDataListToRv()

                startPlaying(item.audio)

                binding.frequencyName.text = item.channelName
                binding.channelHeadline.text = item.albumName
                binding.radioToolbar.alertImage.visibility = View.VISIBLE
                binding.radioToolbar.hdIcon.visibility = View.VISIBLE
                binding.dropIv.visibility = View.VISIBLE
                binding.scrollText.visibility = View.VISIBLE
                binding.scrollText.postDelayed(
                    { binding.scrollText.visibility = View.GONE }, 5000
                )
            } else {
                binding.radioToolbar.alertImage.visibility = View.GONE
                binding.radioToolbar.hdIcon.visibility = View.GONE
                binding.dropIv.visibility = View.GONE
                startPlaying(item.audio)
            }

//        set image
            binding.channelHeadline.text = item.albumName
            Glide.with(this).load(item.channelImg).into(binding.screenView)
        }
    }

    private fun screenChange(currentItem: Int) {
        val item = hdChannelDataItems[currentItem]
        binding.hdChannelRv.visibility = View.GONE
        binding.channelHeadline.visibility = View.VISIBLE
        binding.screenView.setImageResource(item.channelImg)
        binding.screenViewLayout.visibility = View.VISIBLE
        startPlaying(item.audio)
        // HD icon visibility
        // Alert icon visibility
        binding.radioToolbar.hdIcon.isVisible = item.isHDChannel
        binding.radioToolbar.alertImage.isVisible = item.isHDChannel
        binding.channelHeadline.text = item.albumName

//        set tune bar to current item
//        binding.tunebarRv.smoothScrollToPosition(currentItem + 1)
//        set frequnecy Number
//        binding.frequencyName.text = item.channelNum.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    // ***********************  HD  ****************

    private fun addHDDataListToRv() {
        hdChannelDataItems.clear()
        hdChannelDataItems.add(
            ChannelsModel(
                R.drawable.justin_bieber,
                "KNTU 100.2 | HD1",
                "Justin Bieber | @justinbieber | Canada",
                true,
                100.0,
                R.raw.taratata
            )
        )
        hdChannelDataItems.add(
            ChannelsModel(
                R.drawable.taylor_swift,
                "KNTU 100.2 | HD2",
                "Taylor Swift | @taylorswift13 | USA",
                true,
                100.0,
                R.raw.radio
            )
        )
        hdChannelDataItems.add(
            ChannelsModel(
                R.drawable.shakira,
                "KNTU 100.2 | HD3",
                "Shakira | @shakira | Colombia",
                true,
                100.0,
                R.raw.shakira
            )
        )
        hdChannelDataItems.add(
            ChannelsModel(
                R.drawable.jennifer_lopez,
                "KNTU 100.2 | HD4",
                "Jennifer Lopez | @JLo | USA",
                true,
                100.0,
                R.raw.jenifer
            )
        )
        hdChannelDataItems.add(
            ChannelsModel(
                R.drawable.katy_perry,
                "KNTU 100.2 | HD5",
                "Katy Perry | Fleetwood Mac",
                true,
                100.0,
                R.raw.katy
            )
        )
        hdChannelDataItems.add(
            ChannelsModel(
                R.drawable.justin_timberlake,
                "KNTU 100.2 | HD6",
                "Justin Timberlake | @jtimberlake | USA",
                true,
                100.0,
                R.raw.justin
            )
        )
        hdChannelDataItems.add(
            ChannelsModel(
                R.drawable.britney_spears,
                "KNTU 100.2 | HD7",
                "Britney Spears | @britneyspears | USA",
                true,
                100.0,
                R.raw.britney
            )
        )
    }
    private fun addDataListToRv() {
        channelDataItems.clear()
        channelDataItems.add(
            ChannelsModel(
                R.drawable.arrahman,
                "AR Rahuman 50.0 | Non-HD",
                "Rahuman Hits | Normal",
                false,
                50.0,
                R.raw.start
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.waves,
                "No-Frq-Found 51.0 | Non-HD",
                "No Frequency",
                false,
                51.0,
                R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave,
                "No-Frq-Found 52.0 | Non",
                "No Frequency",
                false,
                52.0,
                R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 53.0 | Non", "No Frequency", false, 53.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 54.0 | Non", "No Frequency", false, 54.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.yuvan,
                "Yuvan Trends 55.0 | Non-HD",
                "Yuvan Hits | Normal",
                false,
                55.0,
                R.raw.yuvanhits
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 56.0 | Non", "No Frequency", false, 56.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 57.0 | Non", "No Frequency", false, 57.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 58.0 | Non", "No Frequency", false, 58.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 59.0 | Non", "No Frequency", false, 59.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.ghoshal,
                "Tamil Hits 60.0 | Non-HD",
                "Shreya Ghoshal Hits",
                false,
                60.0,
                R.raw.ghosal
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 61.0 | Non", "No Frequency", false, 61.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 62.0 | Non", "No Frequency", false, 62.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 63.0 | Non", "No Frequency", false, 63.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 64.0 | Non", "No Frequency", false, 64.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.harris,
                "Harris Rain 65.0 | Non-HD",
                "Harris Varighal",
                false,
                65.0,
                R.raw.harrisstar
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 66.0 | Non", "No Frequency", false, 66.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 67.0 | Non", "No Frequency", false, 67.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 68.0 | Non", "No Frequency", false, 68.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 69.0 | Non", "No Frequency", false, 69.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.ravi,
                "Ravi Hits 70.0 | Non-HD",
                "Ravi Sticks | Normal",
                false,
                70.0,
                R.raw.ravi
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 71.0 | Non", "No Frequency", false, 71.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 72.0 | Non", "No Frequency", false, 72.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 73.0 | Non", "No Frequency", false, 73.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 74.0 | Non", "No Frequency", false, 74.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.srinivas,
                "Srinivas Rhythems 75.0 | Non-HD",
                "Srinivas Feelings | Normal",
                false,
                75.0,
                R.raw.srinivas
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 76.0 | Non", "No Frequency", false, 76.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 77.0 | Non", "No Frequency", false, 77.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 78.0 | Non", "No Frequency", false, 78.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 79.0 | Non", "No Frequency", false, 79.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.shreya,
                "Shreya Collections 80.0 | Non-HD",
                "Shreya Collection | Normal",
                false,
                80.0,
                R.raw.shereya
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 81.0 | Non", "No Frequency", false, 81.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 82.0 | Non", "No Frequency", false, 82.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 83.0 | Non", "No Frequency", false, 83.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 84.0 | Non", "No Frequency", false, 84.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.america_journey,
                "Journey to America FM 85.0 | Non",
                "Journey | Normal",
                false,
                85.0,
                R.raw.america
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 86.0 | Non", "No Frequency", false, 86.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 87.0 | Non", "No Frequency", false, 87.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 88.0 | Non", "No Frequency", false, 88.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 89.0 | Non", "No Frequency", false, 89.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.party,
                "Every day Party Hall 90.0 | Non-std",
                "Party Hall | Normal",
                false,
                90.0,
                R.raw.party
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 91.0 | Non", "No Frequency", false, 91.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 92.0 | Non", "No Frequency", false, 92.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 93.0 | Non", "No Frequency", false, 93.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 94.0 | Non", "No Frequency", false, 94.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.dragon_girl,
                "Game of Throne FM 95.0 | Non",
                "Daenerys Targaryen | Emilia Clarke",
                false,
                95.0,
                R.raw.dragon
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 96.0 | Non", "No Frequency", false, 96.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 97.0 | Non", "No Frequency", false, 97.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 98.0 | Non", "No Frequency", false, 98.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 99.0 | Non", "No Frequency", false, 99.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.shakira,
                "KNTU 100.0 | HD",
                "Shakira | @shakira | Colombia",
                true,
                100.0,
                R.raw.shaki
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 101.0 | Non", "No Frequency", false, 101.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 102.0 | Non", "No Frequency", false, 102.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 103.0 | Non", "No Frequency", false, 103.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 104.0 | Non", "No Frequency", false, 104.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.arya_stark,
                "Arya Sark Screw 105.0 | Non-HD",
                "Arya Stark | GOT",
                false,
                105.0,
                R.raw.arya
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 106.0 | Non", "No Frequency", false, 106.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 107.0 | Non", "No Frequency", false, 107.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 108.0 | Non", "No Frequency", false, 108.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 109.0 | Non", "No Frequency", false, 109.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.ronaldo,
                "Football Ronaldo 110.0 | Non",
                "Portughual | Normal",
                false,
                110.0,
                R.raw.taratata
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 111.0 | Non", "No Frequency", false, 111.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 112.0 | Non", "No Frequency", false, 112.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 113.0 | Non", "No Frequency", false, 113.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 114.0 | Non", "No Frequency", false, 114.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.john_snow,
                "John Snow Dragon 115.0 | Non",
                "Dragon Son | Normal",
                false,
                115.0,
                R.raw.jhonsnow
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave,
                "NORMAL 116.0 | Non - No Frq",
                "No Frequency",
                false,
                116.0,
                R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 117.0 | Non", "No Frequency", false, 117.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 118.0 | Non", "No Frequency", false, 118.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 119.0 | Non", "No Frequency", false, 119.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.kylian,
                "Football Kylyan MBabbe 120.0 | Non-HD",
                "France | Normal",
                false,
                120.0,
                R.raw.kylian
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 121.0 | Non", "No Frequency", false, 121.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 122.0 | Non", "No Frequency", false, 122.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 123.0 | Non", "No Frequency", false, 123.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 124.0 | Non", "No Frequency", false, 124.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.micheal,
                "Prison Break Drauma 125.0 | Non",
                "Wentworth Miller | Michael Scofield",
                false,
                125.0,
                R.raw.michel
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 126.0 | Non", "No Frequency", false, 126.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 127.0 | Non", "No Frequency", false, 127.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 128.0 | Non", "No Frequency", false, 128.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 129.0 | Non", "No Frequency", false, 129.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.lincoln,
                "Lincoln Burrows FM 130.0 | Non",
                "Dominic Purcell | Lincoln Burrows",
                false,
                130.0,
                R.raw.lincoln
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 131.0 | Non", "No Frequency", false, 131.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 132.0 | Non", "No Frequency", false, 132.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 133.0 | Non", "No Frequency", false, 133.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 134.0 | Non", "No Frequency", false, 134.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.sara,
                "Sarah Wayne Callies 135.0 | Non",
                "Sara Tancredi | Normal",
                false,
                135.0,
                R.raw.sara
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 136.0 | Non", "No Frequency", false, 136.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 137.0 | Non", "No Frequency", false, 137.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 138.0 | Non", "No Frequency", false, 138.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 139.0 | Non", "No Frequency", false, 139.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.rock, "WWE 140.0 | Non", "Wrestler | Rocky Man", false, 140.0, R.raw.rock
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 141.0 | Non", "No Frequency", false, 141.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 142.0 | Non", "No Frequency", false, 142.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 143.0 | Non", "No Frequency", false, 143.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 144.0 | Non", "No Frequency", false, 144.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.captain,
                "Marvel Studio 145.0 | Non-HD",
                "Captain America | Marvel Studio",
                false,
                145.0,
                R.raw.captain
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 146.0 | Non", "No Frequency", false, 146.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 147.0 | Non", "No Frequency", false, 147.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 148.0 | Non", "No Frequency", false, 148.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 149.0 | Non", "No Frequency", false, 149.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.hulk,
                "Hulk Atrocities 150.0 | Non-HD",
                "Marvel Studio | Hulk Man",
                false,
                150.0,
                R.raw.hulk
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 151.0 | Non", "No Frequency", false, 151.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 152.0 | Non", "No Frequency", false, 152.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 153.0 | Non", "No Frequency", false, 153.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 154.0 | Non", "No Frequency", false, 154.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.ironman,
                "Welcome to Digital World FM 155.0 | Non-HD",
                "Avengers | Marvel Studio",
                false,
                155.0,
                R.raw.iron
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 156.0 | Non", "No Frequency", false, 156.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 157.0 | Non", "No Frequency", false, 157.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 158.0 | Non", "No Frequency", false, 158.0, R.raw.emptyfreq
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 159.0 | Non", "No Frequency", false, 159.0, R.raw.emptyfreq
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.jeni,
                "Jennifer Lawrence 160.0 | Non-HD",
                "Jennifer | American actress",
                false,
                160.0,
                R.raw.lawrence
            )
        )
        //***********************
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 51.0 | Non", "No Frequency", false, 161.0, R.raw.radio
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 52.0 | Non", "No Frequency", false, 162.0, R.raw.radio
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 53.0 | Non", "No Frequency", false, 163.0, R.raw.radio
            )
        )
        channelDataItems.add(
            ChannelsModel(
                R.raw.d_wave, "NORMAL 54.0 | Non", "No Frequency", false, 164.0, R.raw.radio
            )
        )
        //************************
        channelDataItems.add(
            ChannelsModel(
                R.drawable.jennifer_lopez,
                "Jennifer Lopez 165.0 | Non-HD",
                "Best Actress | World",
                false,
                165.0,
                R.raw.jenilast
            )
        )
    }
}

