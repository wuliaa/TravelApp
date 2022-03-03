package com.example.travelapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.travelapp.MyApplication
import com.example.travelapp.R
import com.example.travelapp.bean.CommentVo
import java.text.SimpleDateFormat

class ReplyAdapter: BaseExpandableListAdapter() {
    private var commentList: MutableList<CommentVo> = ArrayList()
    private var replyList: MutableList<CommentVo> = ArrayList()
    private var context: Context? = null

    fun setContext(context: Context){
        this.context = context
    }

    fun setCommentList(list: List<CommentVo>){
        commentList.clear()
        commentList.addAll(list)
    }

    fun setReplyList(list: List<CommentVo>){
        replyList.clear()
        replyList.addAll(list)
        commentList[0].child.clear()
        commentList[0].child.addAll(replyList)
    }
    override fun getGroupCount(): Int {
        return commentList.size
    }

    override fun getChildrenCount(p0: Int): Int {
        return if(commentList[p0].child == null){
            0
        }else{
            commentList[p0].child.size
        }
    }

    override fun getGroup(p0: Int): Any {
        return commentList[p0]
    }

    override fun getChild(p0: Int, p1: Int): Any {
        return commentList[p0].child[p1]
    }

    override fun getGroupId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getChildId(p0: Int, p1: Int): Long {
        return getCombinedChildId(p0.toLong(), p1.toLong())
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(p0: Int, p1: Boolean, p2: View?, p3: ViewGroup): View {
        val groupHolder: GroupHolder
        var convertView = p2
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                R.layout.comment_item_layout,
                p3,
                false
            )
            groupHolder = GroupHolder(convertView)
            convertView.tag = groupHolder
        } else {
            groupHolder = convertView.tag as GroupHolder
        }
        val url = "${
            MyApplication.getContext()
                ?.getString(R.string.serverBasePath)}images/${commentList[p0].user.userHead}"
        context?.let {
            Glide.with(it)
                .load(url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.ic_place_holder)
                .error(R.drawable.ic_photo_error)
                .into(groupHolder.head)
        }
        groupHolder.nickname.text = commentList[p0].user.nickname
        val pattern = "MM-dd HH:mm"
        groupHolder.time.text = "${SimpleDateFormat(pattern).format(commentList[p0].createTime)}"
        groupHolder.content.text = commentList[p0].content
        return convertView!!
    }

    override fun getChildView(p0: Int, p1: Int, p2: Boolean, p3: View?, p4: ViewGroup?): View {
        val childHolder: ChildHolder
        var convertView =p3
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(R.layout.comment_reply_item_layout, p4, false)
            childHolder = ChildHolder(convertView)
            convertView.tag = childHolder
        } else {
            childHolder = convertView.tag as ChildHolder
        }
        childHolder.name.text = "${commentList[p0].child[p1].user.nickname} 回复 ${commentList[p0].child[p1].parentNickname} :"
        childHolder.content.text = commentList[p0].child[p1].content
        return convertView!!
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    inner class GroupHolder(view: View){
        var head: ImageView = view.findViewById(R.id.comment_item_head)
        var nickname: TextView = view.findViewById(R.id.comment_item_userName)
        var time: TextView = view.findViewById(R.id.comment_item_time)
        var content: TextView = view.findViewById(R.id.comment_item_content)
    }

    inner class ChildHolder(view: View){
        var name: TextView = view.findViewById(R.id.reply_item_user)
        var content: TextView = view.findViewById(R.id.reply_item_content)
    }

    fun addTheReplyData(commentVo: CommentVo) {
        if (commentList[0].child != null) {
            commentList[0].child.add(commentVo)
        } else {
            val replyList: MutableList<CommentVo> = ArrayList()
            replyList.add(commentVo)
            commentList[0].child = replyList
        }
        notifyDataSetChanged()
    }
}