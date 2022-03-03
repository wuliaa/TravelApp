package com.example.travelapp.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.travelapp.LiveDataBus
import com.example.travelapp.R
import com.example.travelapp.base.BaseActivity
import com.example.travelapp.utils.SavePhoto
import com.example.travelapp.utils.UserUtils
import com.example.travelapp.utils.XToastUtils
import com.example.travelapp.viewmodel.RecognitionViewModel
import com.xuexiang.xui.widget.imageview.RadiusImageView
import org.json.JSONObject
import org.w3c.dom.Text

class RecognitionActivity : BaseActivity() {
    private var photo: ImageView? = null
    private var landmarkName: TextView? = null
    private var introduction: TextView? = null
    private var recognitionViewModel: RecognitionViewModel? = null
    private var savePhoto: SavePhoto? = null
    private var filePath: String? = null
    private var progressBar:ProgressBar?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition)
        recognitionViewModel = ViewModelProvider(this).get(RecognitionViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        initView()
    }

    private fun initView() {
        setBack()
        setTitle("地标识别")
        photo = findViewById(R.id.recognized_photo)
        landmarkName = findViewById(R.id.landmark_name)
        introduction = findViewById(R.id.introduction)
        progressBar = findViewById(R.id.recognized_progressbar)
        progressBar?.visibility = View.INVISIBLE
        //LiveDataBus在onCreate和onStop失效
        LiveDataBus.with<Bitmap>("landmark_recognize_photo")
            .observe(this::getLifecycle) {
                recognitionViewModel?.setBitmap(it)
            }
        LiveDataBus.with<String>("landmark_recognize_take_photo")
            .observe(this::getLifecycle) {
                photo?.let { it1 ->
                    Glide.with(this)
                        .load(it)
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                        .placeholder(R.drawable.ic_place_holder)
                        .error(R.drawable.ic_photo_error)
                        .into(it1)
                    //百度地标识别
                    recognitionViewModel?.baiduLandmark(it)
                    filePath = it
                }
                progressBar?.visibility = View.VISIBLE
            }
        recognitionViewModel?.getBitmap()?.observe(this, {
            photo?.let { it1 ->
                Glide.with(this)
                    .load(it)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                    .placeholder(R.drawable.ic_place_holder)
                    .error(R.drawable.ic_photo_error)
                    .into(it1)
                savePhoto = SavePhoto(applicationContext)
                savePhoto?.saveImageToGallery(it)?.let { it2 ->
                    //百度地标识别
                    recognitionViewModel?.baiduLandmark(it2)
                    filePath = it2
                    //阿里地标识别
                    //recognitionViewModel?.aliLandmark(it2)
                }
            }
        })
        recognitionViewModel?.getResult()?.observe(this) { it ->
            val json = JSONObject(it).getJSONObject("result")
            json.getString("landmark").let { v ->
                if(v == ""){
                    progressBar?.visibility = View.INVISIBLE
                    XToastUtils.info("识别不出该图片")
                }else{
                    landmarkName?.text = v
                    recognitionViewModel?.crawl(v)
                }
            }
        }
        recognitionViewModel?.getIntroduction()?.observe(this) {
            progressBar?.visibility = View.INVISIBLE
            introduction?.text = it
            recognitionViewModel?.addRecognition(UserUtils.userid!!,
                filePath!!,landmarkName!!.text.toString(),it)
        }
        recognitionViewModel?.getNoNet()?.observe(this, {
            progressBar?.visibility = View.INVISIBLE
            XToastUtils.error(it)
        })
    }
}