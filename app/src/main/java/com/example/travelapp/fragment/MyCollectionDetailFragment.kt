package com.example.travelapp.fragment

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import com.example.travelapp.activity.ChatActivity
import com.example.travelapp.adapter.CommentAdapter
import com.example.travelapp.adapter.NineGridRecycleAdapter
import com.example.travelapp.base.BaseFragment
import com.example.travelapp.bean.CommentVo
import com.example.travelapp.bean.GuidelineVo
import com.example.travelapp.customview.CommentExpandableListView
import com.example.travelapp.customview.MyBottomSheetDialog
import com.example.travelapp.customview.MyBottomSheetDialog2
import com.example.travelapp.utils.EPSoftKeyBoardListener
import com.example.travelapp.utils.UserUtils
import com.example.travelapp.utils.XToastUtils
import com.example.travelapp.viewmodel.MyCollectionViewModel
import com.xuexiang.xui.adapter.recyclerview.XLinearLayoutManager
import com.xuexiang.xui.widget.button.shinebutton.ShineButton
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.xuexiang.xui.widget.imageview.RadiusImageView
import com.xuexiang.xutil.app.ActivityUtils
import java.text.SimpleDateFormat

class MyCollectionDetailFragment : BaseFragment(), GeocodeSearch.OnGeocodeSearchListener,
    WeatherSearch.OnWeatherSearchListener, ShineButton.OnCheckedChangeListener {
    private var myCollectionViewModel: MyCollectionViewModel? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: NineGridRecycleAdapter? = null
    private var back: ImageView? = null
    private var protrait: ImageView? = null
    private var nickname: TextView? = null
    private var title: TextView? = null
    private var content: TextView? = null
    private var address: TextView? = null
    private var map: RadiusImageView? = null
    private var shineButton: ShineButton? = null
    private var starText: TextView? = null
    private var weather: TextView? = null
    private var time: TextView? = null
    private var contact: ImageView? = null
    private var contactText: TextView? = null
    private var expandableListView: CommentExpandableListView? = null
    private var commentAdapter: CommentAdapter? = null
    private var noComments: TextView? = null
    private var commentEditText: EditText? = null
    private var commentButton: Button? = null
    private var parentId: Long? = null
    private var parentNickname: String? = null
    private var position: Int? = null
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_collection_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myCollectionViewModel =
            ViewModelProvider(requireActivity()).get(MyCollectionViewModel::class.java)
        initViews()
    }

    override fun onResume() {
        super.onResume()
        shineButton?.isChecked = true
        myCollectionViewModel?.getPics()?.observe(this, {
            mAdapter?.refresh(it)
            mAdapter?.notifyDataSetChanged()
        })
        myCollectionViewModel?.getItem()?.observe(this, {
            myCollectionViewModel?.setImgs(it.photo)
            myCollectionViewModel?.guidelineId = it.id
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
            myCollectionViewModel?.getComments()
            //不能与自己聊天
            userId = it.userId
        })

        myCollectionViewModel?.getCommentList()?.observe(this, {
            if (it.isEmpty()) {
                noComments?.visibility = View.VISIBLE
            } else {
                noComments?.visibility = View.GONE
            }
            initExpandableListView(it)
        })
        myCollectionViewModel?.getCommentSuccess()?.observe(this, {
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
    }

    private fun initViews() {
        back = requireActivity().findViewById(R.id.collection_detail_back)
        protrait = requireActivity().findViewById(R.id.collection_detail_avatar)
        nickname = requireActivity().findViewById(R.id.collection_detail_nickname)
        title = requireActivity().findViewById(R.id.collection_detail_title)
        content = requireActivity().findViewById(R.id.collection_detail_content)
        address = requireActivity().findViewById(R.id.collection_detail_address)
        map = requireActivity().findViewById(R.id.collection_map)
        starText = requireActivity().findViewById(R.id.collection_detail_star_text)
        weather = requireActivity().findViewById(R.id.collection_detail_weather)
        time = requireActivity().findViewById(R.id.collection_detail_create_time)
        shineButton = requireActivity().findViewById(R.id.collection_shine_button)
        shineButton?.setOnCheckStateChangeListener(this)
        contact = requireActivity().findViewById(R.id.collection_detail_contact_imageView)
        contactText = requireActivity().findViewById(R.id.collection_detail_contact_text)
        expandableListView = requireActivity().findViewById(R.id.collection_detail_page_lv_comment)
        noComments = requireActivity().findViewById(R.id.collection_no_comments)
        commentEditText = requireActivity().findViewById(R.id.collection_comment_edittext)
        commentButton = requireActivity().findViewById(R.id.collection_comment_btn_send)
        //确保调用SDK任何接口前先调用更新隐私合规updatePrivacyShow、updatePrivacyAgree两个接口并且参数值都为true
        //隐私政策合规
        ServiceSettings.updatePrivacyShow(context, true, true)
        ServiceSettings.updatePrivacyAgree(context, true)
        back?.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, MyCollectionFragment())
                .commit()
        }
        initRecyclerView()
        map?.setOnClickListener {
            address?.text?.let {
                getLat(it.toString())
            }
        }
        myCollectionViewModel?.getLatLonPoint()?.observe(this, {
            address?.text?.let { s ->
                val list = s.toString().split(" ")
                navigation(list[1], context, 0.0, 0.0, it.latitude, it.longitude)
            }
        })
        myCollectionViewModel?.getCantNavi()?.observe(this, {
            XToastUtils.error(it)
        })
        starText?.setOnClickListener {
            shineButton?.callOnClick()
        }
        myCollectionViewModel?.getAddDelete()?.observe(this, {
            if (it) {
                XToastUtils.success("收藏成功")
            } else {
                XToastUtils.info("取消收藏")
            }
        })
        weather?.setOnClickListener {
            address?.text?.let { s ->
                val list = s.toString().split(" ")
                //检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
                val mquery = WeatherSearchQuery(list[0], WeatherSearchQuery.WEATHER_TYPE_LIVE)
                val weatherSearch = WeatherSearch(context)
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
                    .value = myCollectionViewModel?.getItem()?.value
                ActivityUtils.startActivity(ChatActivity::class.java)
            }
        }
        contactText?.setOnClickListener {
            if (userId == UserUtils.userid) {
                XToastUtils.info("无法与自己聊天")
            } else {
                LiveDataBus.with<GuidelineVo>("chat_guideline")
                    .value = myCollectionViewModel?.getItem()?.value
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
                myCollectionViewModel?.addComment(commentEditText?.text.toString())
            } else {
                myCollectionViewModel?.addReply(
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
            requireActivity(),
            object : EPSoftKeyBoardListener.OnSoftKeyBoardChangeListener {
                override fun keyBoardShow(height: Int) {}
                override fun keyBoardHide(height: Int) {
                    commentEditText?.hint = "说点什么吧..."
                }
            })
    }

    private fun initRecyclerView() {
        mRecyclerView = requireActivity().findViewById(R.id.collection_detail_recyclerView)
        mRecyclerView?.layoutManager = XLinearLayoutManager(context)
        mRecyclerView?.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        mRecyclerView?.adapter = NineGridRecycleAdapter().also { mAdapter = it }
    }

    private fun getLat(name: String) {
        val geocoderSearch = GeocodeSearch(context)
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

    override fun onRegeocodeSearched(p0: RegeocodeResult?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
        if (p1 == 1000) {
            if (p0?.geocodeAddressList?.size == 0) {
                myCollectionViewModel?.setCantNavi("该地址无法导航")
            } else {
                p0?.geocodeAddressList?.get(0)?.latLonPoint?.let {
                    myCollectionViewModel?.setLatLonPoint(it)
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
                context?.let {
                    MaterialDialog.Builder(it)
                        .iconRes(R.drawable.ic_baseline_wb_sunny_24)
                        .title(R.string.weather)
                        .content(weatherInfo)
                        .show()
                }
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
            myCollectionViewModel?.addCollection()
        } else {
            myCollectionViewModel?.deleteCollection()
        }
    }

    /**
     * 初始化评论和回复列表
     */
    private fun initExpandableListView(list: List<CommentVo>) {
        expandableListView?.setGroupIndicator(null)
        //默认展开所有回复
        commentAdapter = CommentAdapter()
        context?.let { commentAdapter?.setContext(it) }
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
                commentEditText?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
                commentEditText?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(commentEditText, 0)
            parentId = item.id
            parentNickname = item.user.nickname
            position = p2
            true
        }
        commentAdapter?.onItemClick = {
            MyBottomSheetDialog2(it).show(
                requireActivity().supportFragmentManager,
                "MyBottomSheetDialog"
            )
        }
    }
}