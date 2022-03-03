package com.example.travelapp.activity

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.amap.api.navi.AmapNaviPage
import com.amap.api.navi.AmapNaviParams
import com.amap.api.navi.AmapNaviType
import com.amap.api.services.core.ServiceSettings
import com.amap.api.services.geocoder.GeocodeQuery
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.weather.LocalWeatherForecastResult
import com.amap.api.services.weather.LocalWeatherLiveResult
import com.amap.api.services.weather.WeatherSearch
import com.amap.api.services.weather.WeatherSearchQuery
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.travelapp.LiveDataBus
import com.example.travelapp.MyApplication
import com.example.travelapp.R
import com.example.travelapp.adapter.CommentAdapter
import com.example.travelapp.adapter.NineGridRecycleAdapter
import com.example.travelapp.bean.CommentVo
import com.example.travelapp.bean.GuidelineVo
import com.example.travelapp.customview.CommentExpandableListView
import com.example.travelapp.customview.MyBottomSheetDialog
import com.example.travelapp.utils.EPSoftKeyBoardListener
import com.example.travelapp.utils.UserUtils
import com.example.travelapp.utils.XToastUtils
import com.example.travelapp.viewmodel.GuidelineDetailViewModel
import com.xuexiang.xui.adapter.recyclerview.XLinearLayoutManager
import com.xuexiang.xui.widget.button.shinebutton.ShineButton
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.xuexiang.xui.widget.imageview.RadiusImageView
import com.xuexiang.xutil.app.ActivityUtils
import java.text.SimpleDateFormat


class GuidelineDetailActivity : AppCompatActivity(), GeocodeSearch.OnGeocodeSearchListener,
    WeatherSearch.OnWeatherSearchListener, ShineButton.OnCheckedChangeListener {

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: NineGridRecycleAdapter? = null
    private var guidelineDetailViewModel: GuidelineDetailViewModel? = null
    private var back: ImageView? = null
    private var protrait: ImageView? = null
    private var nickname: TextView? = null
    private var title: TextView? = null
    private var content: TextView? = null
    private var address: TextView? = null
    private var map: RadiusImageView? = null
    private var starText: TextView? = null
    private var weather: TextView? = null
    private var time: TextView? = null
    private var contact: ImageView? = null
    private var contactText: TextView? = null
    private var shineButton: ShineButton? = null
    private var item: GuidelineVo? = null
    private var expandableListView: CommentExpandableListView? = null
    private var commentAdapter: CommentAdapter? = null
    private var noComments: TextView? = null
    private var commentEditText: EditText? = null
    private var commentButton: Button? = null
    private var parentId: Long? = null
    private var parentNickname: String? = null
    private var position: Int? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.guideline_detail_comment)
        guidelineDetailViewModel = ViewModelProvider(this).get(GuidelineDetailViewModel::class.java)
        back = findViewById(R.id.guide_detail_back)
        protrait = findViewById(R.id.guide_detail_avatar)
        nickname = findViewById(R.id.guide_detail_nickname)
        title = findViewById(R.id.guide_detail_title)
        content = findViewById(R.id.guide_detail_content)
        address = findViewById(R.id.guide_detail_address)
        map = findViewById(R.id.map)
        starText = findViewById(R.id.guide_detail_star_text)
        weather = findViewById(R.id.guide_detail_weather)
        time = findViewById(R.id.guide_detail_create_time)
        contact = findViewById(R.id.guide_detail_contact_imageView)
        contactText = findViewById(R.id.guide_detail_contact_text)
        shineButton = findViewById(R.id.shine_button)
        shineButton?.setOnCheckStateChangeListener(this)
        expandableListView = findViewById(R.id.detail_page_lv_comment)
        noComments = findViewById(R.id.no_comments)
        commentEditText = findViewById(R.id.comment_edittext)
        commentButton = findViewById(R.id.comment_btn_send)
    }

    private fun initViews() {
        initRecyclerView()
        //确保调用SDK任何接口前先调用更新隐私合规updatePrivacyShow、updatePrivacyAgree两个接口并且参数值都为true
        //隐私政策合规
        ServiceSettings.updatePrivacyShow(this, true, true)
        ServiceSettings.updatePrivacyAgree(this, true)
        back?.setOnClickListener { finish() }
        map?.setOnClickListener {
            address?.text?.let {
                getLat(it.toString())
            }
        }
        guidelineDetailViewModel?.getLatLonPoint()?.observe(this, {
            address?.text?.let { s ->
                val list = s.toString().split(" ")
                navigation(list[1], this, 0.0, 0.0, it.latitude, it.longitude)
            }
        })
        guidelineDetailViewModel?.getCantNavi()?.observe(this, {
            XToastUtils.error(it)
        })
        guidelineDetailViewModel?.getStar()?.observe(this, {
            shineButton?.isChecked = it
        })
        guidelineDetailViewModel?.getCheck()?.observe(this, {
            if (it) {
                XToastUtils.success("收藏成功")
            } else {
                XToastUtils.info("取消收藏")
            }
        })
        starText?.setOnClickListener {
            shineButton?.callOnClick()
        }
        weather?.setOnClickListener {
            address?.text?.let { s ->
                val list = s.toString().split(" ")
                //检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
                val mquery = WeatherSearchQuery(list[0], WeatherSearchQuery.WEATHER_TYPE_LIVE)
                val weatherSearch = WeatherSearch(this)
                weatherSearch.setOnWeatherSearchListener(this)
                weatherSearch.query = mquery
                weatherSearch.searchWeatherAsyn() //异步搜索
            }
        }
        contact?.setOnClickListener {
            if (userId == UserUtils.userid) {
                XToastUtils.info("无法与自己聊天")
            } else {
                LiveDataBus.with<GuidelineVo>("chat_guideline")
                    .value = item
                ActivityUtils.startActivity(ChatActivity::class.java)
            }
        }
        contactText?.setOnClickListener {
            if (userId == UserUtils.userid) {
                XToastUtils.info("无法与自己聊天")
            } else {
                LiveDataBus.with<GuidelineVo>("chat_guideline")
                    .value = item
                ActivityUtils.startActivity(ChatActivity::class.java)
            }
        }
        commentButton?.isEnabled = false
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                commentButton?.isEnabled =
                    commentEditText?.text.toString().isNotEmpty()
            }

            override fun afterTextChanged(p0: Editable?) {}
        }
        commentEditText?.addTextChangedListener(textWatcher)
        commentButton?.setOnClickListener {
            if (parentId == null) {
                guidelineDetailViewModel?.addComment(commentEditText?.text.toString())
            } else {
                guidelineDetailViewModel?.addReply(
                    commentEditText?.text.toString(),
                    parentId!!,
                    parentNickname!!
                )
            }
            parentId = null
            parentNickname = null
            commentEditText?.setText("")
        }

        EPSoftKeyBoardListener.setListener(
            this,
            object : EPSoftKeyBoardListener.OnSoftKeyBoardChangeListener {
                override fun keyBoardShow(height: Int) {}
                override fun keyBoardHide(height: Int) {
                    commentEditText?.hint = "说点什么吧..."
                }
            })
    }

    private fun initRecyclerView() {
        mRecyclerView = findViewById(R.id.guide_detail_recyclerView)
        mRecyclerView?.layoutManager = XLinearLayoutManager(this)
        mRecyclerView?.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        mRecyclerView?.adapter = NineGridRecycleAdapter().also { mAdapter = it }
    }

    override fun onResume() {
        super.onResume()
        guidelineDetailViewModel?.getPics()?.observe(this, {
            mAdapter?.refresh(it)
            mAdapter?.notifyDataSetChanged()
        })
        guidelineDetailViewModel?.getNoNet()?.observe(this, {
            XToastUtils.error(it)
        })
        guidelineDetailViewModel?.getCommentList()?.observe(this, {
            if (it.isEmpty()) {
                noComments?.visibility = View.VISIBLE
            } else {
                noComments?.visibility = View.GONE
            }
            initExpandableListView(it)
        })
        guidelineDetailViewModel?.getCommentSuccess()?.observe(this, {
            if (it.parentId == null) {
                XToastUtils.success("评论成功")
                noComments?.visibility = View.GONE
                commentAdapter?.addTheCommentData(it)
                commentAdapter?.notifyDataSetChanged()
            } else {
                XToastUtils.success("回复成功")
                position?.let { it1 -> commentAdapter?.addTheReplyData(it, it1) }
                commentAdapter?.notifyDataSetChanged()
            }
        })
        LiveDataBus.with<GuidelineVo>("guideline")
            .observe(this::getLifecycle) {
                Glide.with(this)
                    .load(
                        "${
                            MyApplication.getContext()
                                ?.getString(R.string.serverBasePath)
                        }images/${it.portrait}"
                    )
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.ic_place_holder)
                    .error(R.drawable.ic_photo_error)
                    .into(protrait!!)
                nickname?.text = it.nickname
                title?.text = it.title
                content?.text = it.content
                address?.text = it.landmarkName
                val pattern = "yyyy-MM-dd HH:mm:ss"
                time?.text = "发布于 ${SimpleDateFormat(pattern).format(it.createTime)}"
                guidelineDetailViewModel?.setImgs(it.photo)
                guidelineDetailViewModel?.httpGetIsStar(it.id)
                guidelineDetailViewModel?.getComments()
                item = it
                //不能与自己聊天
                userId = it.userId
            }
        initViews()
    }

    /**
     * 路线规划
     *
     * @param slat 起点纬度
     * @param slon 起点经度
     * @param dlat 终点纬度
     * @param dlon 终点经度
     */
    fun navigation(
        name: String,
        context: Context?,
        slat: Double,
        slon: Double,
        dlat: Double,
        dlon: Double
    ) {
        var start: Poi? = null
        //如果设置了起点
        if (slat != 0.0 && slon != 0.0) {
            start = Poi("起点名称", LatLng(slat, slon), "")
        }
        val end = Poi(name, LatLng(dlat, dlon), "")
        val params = AmapNaviParams(start, null, end, AmapNaviType.DRIVER)
        params.setUseInnerVoice(true)
        params.isMultipleRouteNaviMode = true
        params.isNeedDestroyDriveManagerInstanceWhenNaviExit = true
        //发起导航
        AmapNaviPage.getInstance().showRouteActivity(context, params, null)
    }

    private fun getLat(name: String) {
        val geocoderSearch = GeocodeSearch(this)
        geocoderSearch.setOnGeocodeSearchListener(this)
        // name表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode
        if (name.contains(" ")) {
            val list = name.split(" ")
            val query = GeocodeQuery(list[1], list[0])
            geocoderSearch.getFromLocationNameAsyn(query)
        } else {
            val query = GeocodeQuery(name, name)
            geocoderSearch.getFromLocationNameAsyn(query)
        }
    }

    override fun onRegeocodeSearched(p0: RegeocodeResult?, p1: Int) {

    }

    override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
        if (p1 == 1000) {
            if (p0?.geocodeAddressList?.size == 0) {
                guidelineDetailViewModel?.setCantNavi("该地址无法导航")
            } else {
                p0?.geocodeAddressList?.get(0)?.latLonPoint?.let {
                    guidelineDetailViewModel?.setLatLonPoint(it)
                }
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

    override fun onCheckedChanged(shineButton: ShineButton?, isChecked: Boolean) {
        if (isChecked) {
            guidelineDetailViewModel?.addCollection()
        } else {
            guidelineDetailViewModel?.deleteCollection()
        }
    }

    /**
     * 初始化评论和回复列表
     */
    private fun initExpandableListView(list: List<CommentVo>) {
        expandableListView?.setGroupIndicator(null)
        //默认展开所有回复
        commentAdapter = CommentAdapter()
        commentAdapter?.setContext(this)
        commentAdapter?.setCommentList(list)
        expandableListView?.setAdapter(commentAdapter)
        for (i in list.indices) {
            expandableListView?.expandGroup(i)
        }
        expandableListView?.setOnGroupClickListener { expandableListView, view, i, l ->
            val item = commentAdapter?.getGroup(i) as CommentVo
            commentEditText?.hint = "回复 ${item.user.nickname} :"
            commentEditText?.isFocusable = true
            commentEditText?.isFocusableInTouchMode = true
            commentEditText?.requestFocus()
            val imm: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(commentEditText, 0)
            parentId = item.id
            parentNickname = item.user.nickname
            position = i
            true
        }
        expandableListView?.setOnChildClickListener { p0, p1, p2, p3, p4 ->
            val item = commentAdapter?.getChild(p2, p3) as CommentVo
            commentEditText?.hint = "回复 ${item.user.nickname} :"
            commentEditText?.isFocusable = true
            commentEditText?.isFocusableInTouchMode = true
            commentEditText?.requestFocus()
            val imm: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(commentEditText, 0)
            parentId = item.id
            parentNickname = item.user.nickname
            position = p2
            true
        }
        commentAdapter?.onItemClick = {
            MyBottomSheetDialog(it).show(supportFragmentManager, "MyBottomSheetDialog")
        }
    }

    //region 点击隐藏键盘
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (isHideInput(view, ev)) {
                HideSoftInput(view!!.windowToken)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    // 判定是否需要隐藏
    private fun isHideInput(v: View?, ev: MotionEvent): Boolean {
        if (v != null && v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = (left
                    + v.getWidth())
            return !(ev.x > left && ev.x < right && ev.y > top && ev.y < bottom)
        }
        return false
    }

    // 隐藏软键盘
    private fun HideSoftInput(token: IBinder?) {
        if (token != null) {
            val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

}