package com.example.travelapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.travelapp.MyApplication
import com.example.travelapp.R
import com.example.travelapp.bean.GuidelineVo
import com.xuexiang.xui.widget.layout.XUILinearLayout
import kotlin.collections.ArrayList


class GuidelineAdapter :RecyclerView.Adapter<GuidelineAdapter.ViewHolder>() {

    private var list: MutableList<GuidelineVo> = ArrayList()
    private var context:Context?=null
    var onItemClick:((GuidelineVo)->Unit)?=null

    fun loadMore(items:List<GuidelineVo>){
        list.addAll(items)
    }

    fun setList(list:List<GuidelineVo>){
        this.list.clear()
        this.list.addAll(list)
    }

    fun setContext(context: Context){
        this.context = context
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layout:XUILinearLayout = itemView.findViewById(R.id.guideline_item_layout)
        var photo:ImageView = itemView.findViewById(R.id.guideline_photo)
        var portrait:ImageView = itemView.findViewById(R.id.guideline_avatar)
        var nickname:TextView = itemView.findViewById(R.id.guideline_nickname)
        var title:TextView = itemView.findViewById(R.id.guideline_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuidelineAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.guideline_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nickname.text = list[position].nickname
        holder.layout.radius = 20
        holder.title.text = list[position].title
        val url = "${MyApplication.getContext()
            ?.getString(R.string.serverBasePath)}images/${list[position].portrait}"
        val strList: List<String> = list[position].photo.split(",")
        val photo = "${MyApplication.getContext()
            ?.getString(R.string.serverBasePath)}images/${strList[0]}"
        context?.let {
            Glide.with(it)
                .load(url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.ic_place_holder)
                .error(R.drawable.ic_photo_error)
                .into(holder.portrait)
            Glide.with(it)
                .load(photo)
                .placeholder(R.drawable.ic_place_holder)
                .error(R.drawable.ic_photo_error)
                .into(holder.photo)
        }
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(list[position])
        }

    }

}