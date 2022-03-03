package com.example.travelapp.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.services.core.ServiceSettings
import com.amap.api.services.weather.LocalWeatherForecastResult
import com.amap.api.services.weather.LocalWeatherLiveResult
import com.amap.api.services.weather.WeatherSearch
import com.amap.api.services.weather.WeatherSearchQuery
import com.example.travelapp.R
import com.example.travelapp.adapter.CommentAdapter
import com.example.travelapp.adapter.NineGridRecycleAdapter
import com.example.travelapp.base.BaseFragment
import com.example.travelapp.bean.CommentVo
import com.example.travelapp.customview.CommentExpandableListView
import com.example.travelapp.customview.MyBottomSheetDialog2
import com.example.travelapp.customview.MyBottomSheetDialog3
import com.example.travelapp.utils.EPSoftKeyBoardListener
import com.example.travelapp.utils.XToastUtils
import com.example.travelapp.viewmodel.MyGuidelineViewModel
import com.xuexiang.xui.adapter.recyclerview.XLinearLayoutManager
import com.xuexiang.xui.widget.dialog.DialogLoader
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.text.SimpleDateFormat

class MyGuidelineDetailFragment : BaseFragment(), WeatherSearch.OnWeatherSearchListener {
    private var myGuidelineViewModel: MyGuidelineViewModel? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: NineGridRecycleAdapter? = null
    private var title: TextView? = null
    private var content: TextView? = null
    private var address: TextView? = null
    private var weather: TextView? = null
    private var time: TextView? = null
    private var delete: TextView? = null
    private var expandableListView: CommentExpandableListView? = null
    private var commentAdapter: CommentAdapter? = null
    private var noComments: TextView? = null
    private var commentEditText: EditText? = null
    private var commentButton: Button? = null
    private var parentId: Long? = null
    private var parentNickname: String? = null
    private var position: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_guideline_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myGuidelineViewModel =
            ViewModelProvider(requireActivity()).get(MyGuidelineViewModel::class.java)
        initViews()
    }

    private fun initViews() {
        title = requireActivity().findViewById(R.id.my_guide_detail_title)
        content = requireActivity().findViewById(R.id.my_guide_detail_content)
        address = requireActivity().findViewById(R.id.my_guide_detail_address)
        weather = requireActivity().findViewById(R.id.my_guide_detail_weather)
        time = requireActivity().findViewById(R.id.my_guide_detail_create_time)
        delete = requireActivity().findViewById(R.id.my_guide_detail_delete)
        expandableListView = requireActivity().findViewById(R.id.my_guideline_detail_page_lv_comment)
        noComments = requireActivity().findViewById(R.id.my_guideline_no_comments)
        commentEditText = requireActivity().findViewById(R.id.my_guideline_comment_edittext)
        commentButton = requireActivity().findViewById(R.id.my_guideline_comment_btn_send)
        setBack()
        setTitle("攻略详情")
        initRecyclerView()
        //确保调用SDK任何接口前先调用更新隐私合规updatePrivacyShow、updatePrivacyAgree两个接口并且参数值都为true
        //隐私政策合规
        ServiceSettings.updatePrivacyShow(context, true, true)
        ServiceSettings.updatePrivacyAgree(context, true)
        myGuidelineViewModel?.getPics()?.observe(viewLifecycleOwner, {
            mAdapter?.refresh(it)
            mAdapter?.notifyDataSetChanged()
        })
        myGuidelineViewModel?.getItem()?.observe(this, {
            title?.text = it.title
            content?.text = it.content
            address?.text = it.landmarkName
            val pattern = "yyyy-MM-dd HH:mm:ss"
            time?.text = "发布于 ${SimpleDateFormat(pattern).format(it.createTime)}"
            myGuidelineViewModel?.setImgs(it.photo)
            myGuidelineViewModel?.guidelineId = it.id
            myGuidelineViewModel?.getComments()
        })
        myGuidelineViewModel?.getCommentList()?.observe(this, {
            if (it.isEmpty()) {
                noComments?.visibility = View.VISIBLE
            } else {
                noComments?.visibility = View.GONE
            }
            initExpandableListView(it)
        })
        myGuidelineViewModel?.getCommentSuccess()?.observe(this, {
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
        delete?.setOnClickListener {
            DialogLoader.getInstance().showConfirmDialog(
                context,
                getString(R.string.check_delete),
                getString(R.string.lab_yes),
                { dialog: DialogInterface, which: Int ->
                    dialog.dismiss()
                    myGuidelineViewModel?.delete()
                },
                getString(R.string.lab_no)
            ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        }

        myGuidelineViewModel?.getRes()?.observe(this, {
            XToastUtils.success(it)
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, MyGuidelineFragment())
                .commit()
        })
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
                myGuidelineViewModel?.addComment(commentEditText?.text.toString())
            } else {
                myGuidelineViewModel?.addReply(
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
        mRecyclerView = requireActivity().findViewById(R.id.my_guide_detail_recyclerView)
        mRecyclerView?.layoutManager = XLinearLayoutManager(context)
        mRecyclerView?.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        mRecyclerView?.adapter = NineGridRecycleAdapter().also { mAdapter = it }
    }

    private fun setBack() {
        tv_back?.visibility = View.VISIBLE
        tv_back?.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, MyGuidelineFragment())
                .commit()
        }
    }

    private fun setTitle(mtitle: String?) {
        tv_title?.visibility = View.VISIBLE
        tv_title?.text = mtitle
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
            MyBottomSheetDialog3(it).show(
                requireActivity().supportFragmentManager,
                "MyBottomSheetDialog"
            )
        }
    }
}