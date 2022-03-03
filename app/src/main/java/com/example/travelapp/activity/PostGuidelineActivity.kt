package com.example.travelapp.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelapp.MainActivity
import com.example.travelapp.R
import com.example.travelapp.adapter.ImageSelectGridAdapter
import com.example.travelapp.utils.NetUtils
import com.example.travelapp.utils.UserUtils
import com.example.travelapp.utils.Utils
import com.example.travelapp.utils.XToastUtils
import com.example.travelapp.viewmodel.GuidelineViewModel
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import com.xuexiang.xui.widget.textview.supertextview.SuperButton
import com.zaaach.citypicker.CityPicker
import com.zaaach.citypicker.adapter.OnPickListener
import com.zaaach.citypicker.model.City
import com.zaaach.citypicker.model.HotCity
import com.zaaach.citypicker.model.LocatedCity


class PostGuidelineActivity : AppCompatActivity(), ImageSelectGridAdapter.OnAddPicClickListener {

    private var recyclerView: RecyclerView? = null
    private var mAdapter: ImageSelectGridAdapter? = null
    private var mSelectList: List<LocalMedia> = ArrayList()
    private var buttonBack: ImageView? = null
    private var titleEdit: EditText? = null
    private var contentEdit: EditText? = null
    private var addressEdit: EditText? = null
    private var buttonPost: SuperButton? = null
    private var guidelineViewModel: GuidelineViewModel? = null
    private var buttonCity: Button? = null
    private var progressBar:ProgressBar?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_guideline)
        guidelineViewModel = ViewModelProvider(this).get(GuidelineViewModel::class.java)
        initViews()
        isPostSuccess()
    }

    private fun initViews() {
        initRecyclerView()
        buttonBack = findViewById(R.id.post_guideline_back)
        buttonBack?.setOnClickListener { finish() }
        titleEdit = findViewById(R.id.post_guideline_title)
        contentEdit = findViewById(R.id.post_guideline_content)
        addressEdit = findViewById(R.id.post_guideline_address)
        buttonPost = findViewById(R.id.btn_post)
        buttonCity = findViewById(R.id.choose_city)
        progressBar = findViewById(R.id.post_progressbar)
        progressBar?.visibility = View.INVISIBLE
        buttonPost?.isEnabled = false
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val t1 = titleEdit?.text.toString().isNotEmpty()
                val t2 = contentEdit?.text.toString().isNotEmpty()
                val t3 = addressEdit?.text.toString().isNotEmpty()
                buttonPost?.isEnabled = t1 and t2 and t3
            }

            override fun afterTextChanged(s: Editable) {}
        }
        titleEdit?.addTextChangedListener(textWatcher)
        contentEdit?.addTextChangedListener(textWatcher)
        addressEdit?.addTextChangedListener(textWatcher)
        buttonPost?.setOnClickListener {
            if (!NetUtils.isConnected(applicationContext)) {
                XToastUtils.error("无网络连接")
            } else if (mSelectList.isEmpty()) {
                XToastUtils.error("未选择图片")
            } else {
                progressBar?.visibility = View.VISIBLE
                guidelineViewModel?.setFiles(mSelectList)
                UserUtils.userid?.let { it1 ->
                    guidelineViewModel?.postGuideline(
                        it1,
                        titleEdit?.text.toString(),
                        contentEdit?.text.toString(),
                        buttonCity?.text.toString(),
                        addressEdit?.text.toString()
                    )
                }
            }
        }
        buttonCity?.setOnClickListener {
            pickCity()
        }

    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.post_recycler_view)
        val manager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        recyclerView?.layoutManager = manager
        recyclerView?.adapter = ImageSelectGridAdapter(this, this).also {
            mAdapter = it
        }
        mAdapter?.setSelectList(mSelectList)
        mAdapter?.setSelectMax(8)
        mAdapter?.setOnItemClickListener { position: Int, v: View? ->
            PictureSelector.create(this)
                .themeStyle(R.style.XUIPictureStyle)
                .openExternalPreview(position, mSelectList)
        }
    }

    private fun isPostSuccess() {
        guidelineViewModel?.getResult()?.observe(this, {
            progressBar?.visibility = View.INVISIBLE
            if (it.getString("code") != "200") {
                XToastUtils.error(it.getString("message"))
            } else {
                XToastUtils.success("发布成功")
                val intentToFinish = Intent(this, MainActivity::class.java)
                intentToFinish.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intentToFinish)
                finish()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    // 图片选择
                    mSelectList = PictureSelector.obtainMultipleResult(data)
                    mAdapter?.setSelectList(mSelectList)
                    mAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onAddPicClick() {
        Utils.getPictureSelector(this)
            .selectionMedia(mSelectList)
            .forResult(PictureConfig.CHOOSE_REQUEST)
    }

    private fun pickCity(){
        val hotCities = ArrayList<HotCity>()
        hotCities.add(HotCity("北京", "北京", "101010100"))
        hotCities.add(HotCity("上海", "上海", "101020100"))
        hotCities.add(HotCity("广州", "广东", "101280101"))
        hotCities.add(HotCity("深圳", "广东", "101280601"))
        hotCities.add(HotCity("杭州", "浙江", "101210101"))

        CityPicker.from(this)
            .enableAnimation(true)
            .setLocatedCity(LocatedCity("广州", "广东", "101280101"))
            .setHotCities(hotCities)	//指定热门城市
            .setOnPickListener(object : OnPickListener {
                override fun onPick(position: Int, data: City?) {
                    buttonCity?.text = data?.name
                }
                override fun onLocate() {}
                override fun onCancel() {
                    XToastUtils.info("取消选择")
                }
            })
            .show()
    }
}