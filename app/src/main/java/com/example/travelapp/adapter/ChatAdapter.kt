package com.example.travelapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.travelapp.MyApplication
import com.example.travelapp.R
import com.example.travelapp.bean.ChatModel

class ChatAdapter :RecyclerView.Adapter<ChatAdapter.ViewHolder>(){

    private var list: MutableList<ChatModel> = ArrayList()
    private var context: Context?=null

    fun setList(list: MutableList<ChatModel>){
        this.list = list
    }

    fun addItem(item:ChatModel){
        list.add(item)
    }

    fun addMore(items:List<ChatModel>){
        list.addAll(0,items)
    }

    fun setContext(context: Context){
        this.context = context
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var leftImageView: ImageView = itemView.findViewById(R.id.left_image)
        var leftContentTextView: TextView = itemView.findViewById(R.id.left_content)
        var leftLayout: LinearLayout = itemView.findViewById(R.id.left_bubble)
        var rightImageView: ImageView = itemView.findViewById(R.id.right_image)
        var rightContentTextView: TextView = itemView.findViewById(R.id.right_content)
        var rightLayout: LinearLayout = itemView.findViewById(R.id.right_bubble)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //布局加载器
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatModel: ChatModel = list[position]
        if (chatModel.type == ChatModel.SEND) {
            holder.leftLayout.visibility = View.GONE
            holder.rightLayout.visibility = View.VISIBLE
            holder.rightContentTextView.text = chatModel.content
            val url = "${
                MyApplication.getContext()
                ?.getString(R.string.serverBasePath)}images/${list[position].portrait}"
            context?.let {
                Glide.with(it)
                    .load(url)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.ic_place_holder)
                    .error(R.drawable.ic_photo_error)
                    .into(holder.rightImageView)
            }
        } else {
            holder.rightLayout.visibility = View.GONE
            holder.leftLayout.visibility = View.VISIBLE
            holder.leftContentTextView.text = chatModel.content
            val url = "${
                MyApplication.getContext()
                    ?.getString(R.string.serverBasePath)}images/${list[position].portrait}"
            context?.let {
                Glide.with(it)
                    .load(url)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.ic_place_holder)
                    .error(R.drawable.ic_photo_error)
                    .into(holder.leftImageView)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}