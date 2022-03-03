package com.example.travelapp.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.travelapp.MyApplication
import com.example.travelapp.R
import com.example.travelapp.base.BaseFragment
import com.example.travelapp.viewmodel.MyRecognitionViewModel
import com.xuexiang.xui.widget.imageview.RadiusImageView
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.text.SimpleDateFormat

class MyRecognitionDetailFragment : BaseFragment() {
    private var myRecognitionViewModel: MyRecognitionViewModel? = null
    private var image: ImageView? = null
    private var title: TextView? = null
    private var content: TextView? = null
    private var time: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_recognition_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myRecognitionViewModel =
            ViewModelProvider(requireActivity()).get(MyRecognitionViewModel::class.java)
        initViews()
    }

    private fun initViews() {
        setBack()
        setTitle("识别详情")
        image = requireActivity().findViewById(R.id.detail_recognized_photo)
        title = requireActivity().findViewById(R.id.detail_landmark_name)
        content = requireActivity().findViewById(R.id.detail_introduction)
        time = requireActivity().findViewById(R.id.detail_time)
        myRecognitionViewModel?.getItem()?.observe(this, {
            title?.text = it.landmarkName
            content?.text = it.landmarkDescribe
            val pattern = "yyyy-MM-dd HH:mm:ss"
            time?.text = "识别于 ${SimpleDateFormat(pattern).format(it.createTime)}"
            image?.let { v ->
                Glide.with(this)
                    .load("${MyApplication.getContext()?.getString(R.string.serverBasePath)}images/${it.photo}")
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                    .placeholder(R.drawable.ic_place_holder)
                    .error(R.drawable.ic_photo_error)
                    .into(v)
            }
        })
    }

    private fun setBack() {
        tv_back?.visibility = View.VISIBLE
        tv_back?.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, MyRecognitionFragment.newInstance())
                .commit()
        }
    }

    private fun setTitle(mtitle: String?) {
        tv_title?.visibility = View.VISIBLE
        tv_title?.text = mtitle
    }

}