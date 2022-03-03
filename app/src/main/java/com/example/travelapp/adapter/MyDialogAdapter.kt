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
import com.example.travelapp.bean.DialogVo
import com.example.travelapp.utils.UserUtils
import java.text.SimpleDateFormat


class MyDialogAdapter: RecyclerView.Adapter<MyDialogAdapter.ViewHolder>() {

    private var list: MutableList<DialogVo> = ArrayList()
    private var context: Context?=null
    var onItemClick:((DialogVo)->Unit)?=null

    fun setList(list: List<DialogVo>){
        this.list.clear()
        this.list.addAll(list)
    }

    fun setContext(context: Context){
        this.context = context
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var head: ImageView = itemView.findViewById(R.id.my_dialog_head)
        var nickname: TextView = itemView.findViewById(R.id.my_dialog_nickname)
        var content: TextView = itemView.findViewById(R.id.my_dialog_content)
        var time: TextView = itemView.findViewById(R.id.my_dialog_time)
        var point: ImageView = itemView.findViewById(R.id.unread)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.mydialog_item,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val url = "${
            MyApplication.getContext()?.getString(R.string.serverBasePath)}images/${item.portrait}"
        context?.let{
            Glide.with(it)
                .load(url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.ic_place_holder)
                .error(R.drawable.ic_photo_error)
                .into(holder.head)
        }
        holder.nickname.text = item.nickname
        holder.content.text = item.content
        val pattern = "MM-dd HH:mm"
        holder.time.text = "${SimpleDateFormat(pattern).format(item.createTime)}"
        if(item.fromUserId!=UserUtils.userid&&item.signFlag!=2){
            holder.point.visibility = View.VISIBLE
        }else{
            holder.point.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}