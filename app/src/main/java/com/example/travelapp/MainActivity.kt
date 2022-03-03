package com.example.travelapp

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.amap.api.services.weather.LocalWeatherForecastResult
import com.amap.api.services.weather.LocalWeatherLiveResult
import com.amap.api.services.weather.WeatherSearch
import com.amap.api.services.weather.WeatherSearchQuery
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.travelapp.activity.*
import com.example.travelapp.adapter.GuidelineAdapter
import com.example.travelapp.adapter.MyItemDecoration
import com.example.travelapp.bean.GuidelineVo
import com.example.travelapp.menu.DrawerAdapter
import com.example.travelapp.menu.DrawerItem
import com.example.travelapp.menu.SimpleItem
import com.example.travelapp.menu.SpaceItem
import com.example.travelapp.utils.*
import com.example.travelapp.viewmodel.MainViewModel
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.xuexiang.xui.widget.dialog.DialogLoader
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.xuexiang.xui.widget.imageview.RadiusImageView
import com.xuexiang.xui.widget.searchview.MaterialSearchView
import com.xuexiang.xui.widget.searchview.MaterialSearchView.SearchViewListener
import com.xuexiang.xutil.app.ActivityUtils
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder


class MainActivity : AppCompatActivity(), DrawerAdapter.OnItemSelectedListener,
    WeatherSearch.OnWeatherSearchListener {
    private val POS_MYRECOGNITION = 0
    private val POS_MYGUIDELINE = 1
    private val POS_MYCOLLECTION = 2
    private val POS_MYDIALOG = 3
    private val POS_LOGOUT = 5
    private var screenTitles: Array<String>? = null
    private var screenIcons: Array<Drawable?>? = null
    private var slidingRootNav: SlidingRootNav? = null
    private var searchView: MaterialSearchView? = null
    private var fab1: FloatingActionButton? = null
    private var fab2: FloatingActionButton? = null
    private val guidelineList: MutableList<GuidelineVo> = ArrayList()
    private var recyclerView: RecyclerView? = null
    private var adapter: GuidelineAdapter? = null
    private var mainViewModel: MainViewModel? = null
    private var queryBack: TextView? = null
    private var weather: TextView? = null
    private var progressBar: ProgressBar? = null
    private var refreshLayout: SmartRefreshLayout? = null
    private var page: Int = 1
    private var floatingActionsMenu:FloatingActionsMenu?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        getAuthority()
        initView(savedInstanceState)
        mainViewModel?.webSocketOpen()
    }

    private fun initView(savedInstanceState: Bundle?) {
        progressBar = findViewById(R.id.main_progressbar)
        progressBar?.visibility = View.VISIBLE
        refreshLayout = findViewById(R.id.main_refreshLayout)
        floatingActionsMenu = findViewById(R.id.multiple_actions_left)
        slideNav(savedInstanceState)

        showPortrait()
        mainViewModel?.unRead()

        weather = findViewById(R.id.weather)
        queryBack = findViewById(R.id.query_back)
        searchView = findViewById(R.id.search_view)
        searchView?.apply {
            setVoiceSearch(false)
            setEllipsize(true)
            setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    refreshLayout?.isEnabled = false
                    progressBar?.visibility = View.VISIBLE
                    mainViewModel?.query(query)
                    queryBack?.visibility = View.VISIBLE
                    weather?.visibility = View.INVISIBLE
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    //Do some magic
                    return false
                }
            })
            setOnSearchViewListener(object : SearchViewListener {
                override fun onSearchViewShown() {
                    //Do some magic
                }

                override fun onSearchViewClosed() {
                    //Do some magic
                }
            })
            setSubmitOnClick(true)
        }

        fab1 = findViewById(R.id.fab1)
        fab2 = findViewById(R.id.fab2)
        fab1?.setOnClickListener {
            DialogLoader.getInstance().showContextMenuDialog(
                this,
                getString(R.string.tip_options),
                R.array.menu_values
            ) { dialog, which ->
                openRecognition(which)
            }
        }
        fab2?.setOnClickListener {
            DialogLoader.getInstance().showConfirmDialog(
                this,
                getString(R.string.check_post_guideline),
                getString(R.string.lab_yes),
                { dialog: DialogInterface, which: Int ->
                    dialog.dismiss()
                    ActivityUtils.startActivity(PostGuidelineActivity::class.java)
                },
                getString(R.string.lab_no)
            ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        }
        initRecyclerView()
        //上拉刷新、下拉加载更多
        refreshLayout?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshLayout.layout.postDelayed({
                    page = 1
                    mainViewModel?.asyncData(page)
                }, 1000)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                refreshLayout.layout.postDelayed({
                    page++
                    mainViewModel?.asyncData(page)
                }, 1000)
            }
        })
    }

    private fun openRecognition(i: Int) {
        when (i) {
            0 -> camera()
            1 -> openAlbum()
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel?.getMoreList()?.observe(this, {
            progressBar?.visibility = View.INVISIBLE
            if (it.isNotEmpty()) {
                if (page == 1) {
                    adapter?.setList(it)
                } else {
                    adapter?.loadMore(it)
                }
                adapter?.notifyDataSetChanged()
            }
            refreshLayout?.finishRefresh()
            refreshLayout?.finishLoadMore()
        })
        mainViewModel?.getList()?.observe(this, {
            progressBar?.visibility = View.INVISIBLE
            adapter?.setList(it)
            adapter?.notifyDataSetChanged()
        })
        queryBack?.setOnClickListener {
            page = 1
            mainViewModel?.asyncData(page)
            queryBack?.visibility = View.INVISIBLE
            weather?.visibility = View.VISIBLE
            refreshLayout?.isEnabled = true
        }
        weather?.setOnClickListener {
            //检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
            val mquery = WeatherSearchQuery("广州", WeatherSearchQuery.WEATHER_TYPE_LIVE)
            val weatherSearch = WeatherSearch(this)
            weatherSearch.setOnWeatherSearchListener(this)
            weatherSearch.query = mquery
            weatherSearch.searchWeatherAsyn() //异步搜索
        }
        mainViewModel?.getNoNet()?.observe(this, {
            progressBar?.visibility = View.INVISIBLE
            XToastUtils.error(it)
        })

    }

    private fun slideNav(savedInstanceState: Bundle?) {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        slidingRootNav = SlidingRootNavBuilder(this)
            .withToolbarMenuToggle(toolbar)
            .withMenuOpened(false)
            .withContentClickableWhenMenuOpened(false)
            .withSavedState(savedInstanceState)
            .withMenuLayout(R.layout.menu_left_drawer)
            .inject()
        screenIcons = loadScreenIcons()
        screenTitles = loadScreenTitles()

        val adapter = DrawerAdapter(
            listOf(
                createItemFor(POS_MYRECOGNITION),
                createItemFor(POS_MYGUIDELINE),
                createItemFor(POS_MYCOLLECTION),
                createItemFor(POS_MYDIALOG),
                SpaceItem(48),
                createItemFor(POS_LOGOUT)
            )
        )
        adapter.setListener(this)
        val list = findViewById<RecyclerView>(R.id.list)
        list.isNestedScrollingEnabled = false
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
    }

    private fun initRecyclerView() {
        mainViewModel?.asyncData(page)
        //适配器布局
        recyclerView = findViewById(R.id.travel_guide_list)
        //瀑布流
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        //设置瀑布流边距
        recyclerView?.addItemDecoration(MyItemDecoration(this, 6, 3))
        recyclerView?.layoutManager = layoutManager
        adapter = GuidelineAdapter()
        adapter?.setList(guidelineList)
        adapter?.setContext(this)
        recyclerView?.adapter = adapter
        adapter?.onItemClick = {
            LiveDataBus.with<GuidelineVo>("guideline").value = it
            ActivityUtils.startActivity(GuidelineDetailActivity::class.java)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val item: MenuItem = menu.findItem(R.id.action_search)
        searchView?.setMenuItem(item)
        return true
    }

    override fun onBackPressed() {
        if (searchView?.isSearchOpen == true) {
            searchView?.closeSearch()
        } else {
            super.onBackPressed()
        }
    }

    private fun showPortrait() {
        val url =
            applicationContext.getString(R.string.serverBasePath) + "images/" + UserUtils.userhead
        val imageView: ImageView = findViewById(R.id.iv_avatar)
        Glide.with(this)
            .load(url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.ic_place_holder)
            .error(R.drawable.ic_photo_error)
            .into(imageView)
        val nickname: TextView = findViewById(R.id.tv_username)
        nickname.text = UserUtils.username
    }

    private fun getAuthority() {
        val PERMISSIONS = arrayOf(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
        //检测是否有读的权限
        val permission = ContextCompat.checkSelfPermission(
            this,
            "android.permission.READ_EXTERNAL_STORAGE"
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有读的权限，去申请写的权限，会弹出对话框
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1)
        }
    }

    override fun onItemSelected(position: Int) {
        slidingRootNav?.closeMenu()
        when (position) {
            POS_MYRECOGNITION -> {
                ActivityUtils.startActivity(MyRecognitionActivity::class.java)
            }
            POS_MYGUIDELINE -> {
                ActivityUtils.startActivity(MyGuidelineActivity::class.java)
            }
            POS_MYCOLLECTION -> {
                ActivityUtils.startActivity(MyCollectionActivity::class.java)
            }
            POS_MYDIALOG -> {
                ActivityUtils.startActivity(MyDialogActivity::class.java)
            }
            POS_LOGOUT ->
                DialogLoader.getInstance().showConfirmDialog(
                    this,
                    getString(R.string.lab_logout_confirm),
                    getString(R.string.lab_yes),
                    { dialog: DialogInterface, which: Int ->
                        dialog.dismiss()
                        UserUtils.handleLogoutSuccess()
                        finish()
                    },
                    getString(R.string.lab_no)
                ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            else -> {
            }
        }
    }

    private fun createItemFor(position: Int): DrawerItem<*>? {
        return SimpleItem(screenIcons!![position], screenTitles!![position])
            .withIconTint(color(R.color.gray_icon))
            .withTextTint(color(R.color.gray_icon))
            .withSelectedIconTint(color(R.color.gray_icon))
            .withSelectedTextTint(color(R.color.gray_icon))
    }

    private fun loadScreenTitles(): Array<String> {
        return resources.getStringArray(R.array.ld_activityScreenTitles)
    }

    private fun loadScreenIcons(): Array<Drawable?> {
        val ta = resources.obtainTypedArray(R.array.ld_activityScreenIcons)
        val icons = arrayOfNulls<Drawable>(ta.length())
        for (i in 0 until ta.length()) {
            val id = ta.getResourceId(i, 0)
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id)
            }
        }
        ta.recycle()
        return icons
    }

    private fun loadScreenIcons2(): Array<Drawable?> {
        val ta = resources.obtainTypedArray(R.array.ld_activityScreenIcons2)
        val icons = arrayOfNulls<Drawable>(ta.length())
        for (i in 0 until ta.length()) {
            val id = ta.getResourceId(i, 0)
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id)
            }
        }
        ta.recycle()
        return icons
    }

    @ColorInt
    private fun color(@ColorRes res: Int): Int {
        return ContextCompat.getColor(this, res)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            // 获得当前得到焦点的View，一般情况下就是EditText
            // （特殊情况就是轨迹求或者实体案件会移动焦点）
            val v: View? = currentFocus
            if (isShouldHideInput(v, ev)) {
                hideSoftInput(v?.windowToken)
            }
            //点击空白处menu收回
            if(floatingActionsMenu?.isExpanded == true){
                floatingActionsMenu?.collapse()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，
     * 来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
     */
    private fun isShouldHideInput(v: View?, event: MotionEvent): Boolean {
        if (v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom: Int = top + v.getHeight()
            val right: Int = (left
                    + v.getWidth())
            // 点击EditText的事件，忽略它。
            return (event.x <= left || event.x >= right
                    || event.y <= top || event.y >= bottom)
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，
        // 第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false
    }

    /**
     * 多种隐藏软件盘方法的其中一种
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun hideSoftInput(token: IBinder?) {
        if (token != null) {
            searchView?.closeSearch()
        }
    }

    private var imageUri: Uri? = null
    private fun camera() {
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        imageUri = CameraUtils.camera(this, externalCacheDir)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, 100)
    }

    private fun openAlbum() {
        val intent = AlbumUtils.openAlbum(this)
        startActivityForResult(intent, 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> if (resultCode == RESULT_OK) {
                val bitmap =
                    BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))
                // post
                LiveDataBus.with<Bitmap>("landmark_recognize_photo")
                    .value = bitmap
                ActivityUtils.startActivity(RecognitionActivity::class.java)
            }
            200 -> if (resultCode == Activity.RESULT_OK) {
                var imagePath: String? = null
                // 判断手机系统版本号
                if (Build.VERSION.SDK_INT >= 19) {
                    // 4.4及以上系统使用这个方法处理图片
                    if (data != null) {
                        imagePath = AlbumUtils.handleImageOnKitKat(data, this)
                    }
                } else {
                    // 4.4以下系统使用这个方法处理图片
                    if (data != null) {
                        imagePath = AlbumUtils.handleImageBeforeKitKat(data, contentResolver)
                    }
                }
                imagePath?.let {
                    LiveDataBus.with<String>("landmark_recognize_take_photo")
                        .value = it
                }
                ActivityUtils.startActivity(RecognitionActivity::class.java)
            }
        }
    }

    override fun onWeatherLiveSearched(p0: LocalWeatherLiveResult?, p1: Int) {
        if (p1 == 1000) {
            if (p0 == null || p0.liveResult == null) {
                XToastUtils.error("无查询结果")
            } else {
                val weatherLive = p0.liveResult
                val weatherInfo =
                    "${weatherLive.reportTime} 发布\n${weatherLive.weather}\n${weatherLive.temperature}°\n${weatherLive.windDirection}风     ${weatherLive.windPower}级\n湿度         ${weatherLive.humidity}%"
                MaterialDialog.Builder(this)
                    .iconRes(R.drawable.ic_baseline_wb_sunny_24)
                    .title(R.string.weather)
                    .content(weatherInfo)
                    .show()
            }
        } else {
            XToastUtils.error("无法查询天气")
        }
    }

    override fun onWeatherForecastSearched(p0: LocalWeatherForecastResult?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogLoader.getInstance().showConfirmDialog(
                this,
                getString(R.string.check_out),
                getString(R.string.lab_yes),
                { dialog: DialogInterface, which: Int ->
                    dialog.dismiss()
                    finish()
                },
                getString(R.string.lab_no)
            ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        }
        return false
    }
}