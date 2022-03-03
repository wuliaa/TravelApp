package com.example.travelapp.customview

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.example.travelapp.R
import com.example.travelapp.adapter.ReplyAdapter
import com.example.travelapp.bean.CommentVo
import com.example.travelapp.utils.EPSoftKeyBoardListener
import com.example.travelapp.utils.XToastUtils
import com.example.travelapp.viewmodel.GuidelineDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_my_bottom_sheet.view.*

class MyBottomSheetDialog(var commentVo: CommentVo) : BottomSheetDialogFragment() {
    private var replyAdapter: ReplyAdapter? = null
    private var guidelineDetailViewModel: GuidelineDetailViewModel? = null
    private var parentId: Long? = null
    private var parentNickname: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_my_bottom_sheet, null)
        dialog.setContentView(view)
        initView(view)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogBg)
        guidelineDetailViewModel = ViewModelProvider(requireActivity()).get(GuidelineDetailViewModel::class.java)
    }

    private fun initView(rootView: View) {
        rootView.dialog_down.setOnClickListener { dismiss() }
        guidelineDetailViewModel?.initCommentList(commentVo.child)
        val list = listOf(commentVo)
        rootView.dialog_detail_comment?.setGroupIndicator(null)
        //默认展开所有回复
        replyAdapter = ReplyAdapter()
        replyAdapter?.setContext(requireContext())
        replyAdapter?.setCommentList(list)
        guidelineDetailViewModel?.list?.let { replyAdapter?.setReplyList(it) }
        rootView.dialog_detail_comment?.setAdapter(replyAdapter)
        for (i in list.indices) {
            rootView.dialog_detail_comment?.expandGroup(i)
        }
        rootView.dialog_detail_comment?.setOnGroupClickListener { expandableListView, view, i, l ->
            val item = replyAdapter?.getGroup(i) as CommentVo
            rootView.reply_edittext?.hint = "回复 ${item.user.nickname}"
            rootView.reply_edittext?.isFocusable = true
            rootView.reply_edittext?.isFocusableInTouchMode = true
            rootView.reply_edittext?.requestFocus()
            val imm: InputMethodManager =
                rootView.reply_edittext.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(rootView.reply_edittext, 0)
            parentId = item.id
            parentNickname = item.user.nickname
            true
        }
        rootView.dialog_detail_comment?.setOnChildClickListener { p0, p1, p2, p3, p4 ->
            val item = replyAdapter?.getChild(p2, p3) as CommentVo
            rootView.reply_edittext?.hint = "回复 ${item.user.nickname}"
            rootView.reply_edittext?.isFocusable = true
            rootView.reply_edittext?.isFocusableInTouchMode = true
            rootView.reply_edittext?.requestFocus()
            val imm: InputMethodManager =
                rootView.reply_edittext.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(rootView.reply_edittext, 0)
            parentId = item.id
            parentNickname = item.user.nickname
            true
        }
        rootView.reply_btn_send?.isEnabled = false
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                rootView.reply_btn_send?.isEnabled =
                    rootView.reply_edittext?.text.toString().isNotEmpty()
            }
            override fun afterTextChanged(p0: Editable?) {}
        }
        rootView.reply_edittext?.addTextChangedListener(textWatcher)
        rootView.reply_btn_send?.setOnClickListener {
            if(parentId == null && parentNickname == null){
                parentId = commentVo.id
                parentNickname = commentVo.user.nickname
            }
            guidelineDetailViewModel?.addReply2(
                rootView.reply_edittext?.text.toString(),
                parentId!!,
                parentNickname!!
            )
            parentId = null
            parentNickname = null
            rootView.reply_edittext?.setText("")
        }

        EPSoftKeyBoardListener.setListener(
            requireActivity(),
            object : EPSoftKeyBoardListener.OnSoftKeyBoardChangeListener {
                override fun keyBoardShow(height: Int) {}
                override fun keyBoardHide(height: Int) {
                    rootView.reply_edittext?.hint = "说点什么吧..."
                }
            })
    }

    override fun onResume() {
        super.onResume()
        guidelineDetailViewModel?.getReplySuccess()?.observe(this, {
            XToastUtils.success("回复成功")
            replyAdapter?.addTheReplyData(it)
            replyAdapter?.notifyDataSetChanged()
            guidelineDetailViewModel?.getComments()
        })
    }
}
