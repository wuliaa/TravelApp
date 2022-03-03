package com.example.travelapp.adapter

import com.example.travelapp.MyApplication
import com.example.travelapp.R
import com.example.travelapp.bean.Recognition
import com.xuexiang.xui.adapter.recyclerview.BaseRecyclerAdapter
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder
import java.text.SimpleDateFormat

class MyRecognitionAdapter : BaseRecyclerAdapter<Recognition>() {

    override fun getItemLayoutId(viewType: Int): Int {
        return R.layout.myrecognition_item
    }

    override fun bindData(holder: RecyclerViewHolder, position: Int,
                          item: Recognition?) {
        item?.let{
            holder.text(R.id.my_recognition_item_title,it.landmarkName)
            holder.text(R.id.my_recognition_item_summary,it.landmarkDescribe)
            val pattern = "yyyy-MM-dd HH:mm:ss"
            holder.text(R.id.my_recognition_item_time,"识别于 ${SimpleDateFormat(pattern).format(it.createTime)}")
            holder.image(R.id.my_recognition_item_image,"${
                MyApplication.getContext()?.getString(R.string.serverBasePath)}images/${it.photo}")
        }
    }
}