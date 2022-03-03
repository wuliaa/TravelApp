package com.example.travelapp.activity

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.travelapp.LiveDataBus
import com.example.travelapp.R
import com.example.travelapp.adapter.ChatAdapter
import com.example.travelapp.base.BaseActivity
import com.example.travelapp.bean.*
import com.example.travelapp.utils.UserUtils
import com.example.travelapp.utils.XToastUtils
import com.example.travelapp.viewmodel.ChatViewModel
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener


class ChatActivity : BaseActivity() {
    private var recyclerView: RecyclerView? = null
    private var chatAdapter: ChatAdapter? = null
    private var editText: EditText? = null
    private var btnSend: Button? = null
    private var toUserPortrait: String? = null
    private var toUserId: String? = null
    private var chatViewModel: ChatViewModel? = null
    private var swipe:SwipeRefreshLayout?=null
    private var page:Int = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        initViews()
    }

    override fun onResume() {
        super.onResume()
        LiveDataBus.with<GuidelineVo>("chat_guideline")
            .observe(this::getLifecycle) {
                setTitle(it.nickname)
                toUserPortrait = it.portrait
                toUserId = it.userId
                chatViewModel?.toUserPortrait = it.portrait
            }
        LiveDataBus.with<DialogVo>("dialog")
            .observe(this::getLifecycle) {
                setTitle(it.nickname)
                toUserPortrait = it.portrait
                toUserId = if (it.fromUserId == UserUtils.userid) {
                    it.toUserId
                } else {
                    it.fromUserId
                }
                chatViewModel?.toUserPortrait = it.portrait
            }
        LiveDataBus.delete("dialog")

        toUserId?.let {
            chatViewModel?.getLastFive(UserUtils.userid!!, it)?.observe(this,{l->
                chatViewModel?.initData(l.asReversed())
            })
        }

        chatViewModel?.getCList()?.observe(this, {
            chatAdapter?.setList(it)
            chatAdapter?.notifyDataSetChanged()
            chatAdapter?.itemCount?.minus(1)?.let { it1 -> recyclerView?.scrollToPosition(it1) }
            page=2
        })
        chatViewModel?.getMoreList()?.observe(this,{
            chatAdapter?.addMore(it)
            chatAdapter?.notifyDataSetChanged()
            swipe?.isRefreshing = false
        })

        LiveDataBus.with<String>("unread_ids")
            .observe(this::getLifecycle) {
                chatViewModel?.changeMsgRead(it)
            }
        chatViewModel?.getNoNet()?.observe(this,{
            swipe?.isRefreshing = false
            XToastUtils.error(it)
        })
    }

    fun initViews() {
        setBack()
        recyclerView = findViewById(R.id.chat_recyclerview)
        editText = findViewById(R.id.chat_edittext)
        btnSend = findViewById(R.id.chat_btn_send)
        btnSend?.isEnabled = false
        swipe = findViewById(R.id.swipe_chat)
        initRecyclerView()
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnSend?.isEnabled = editText?.text.toString().isNotEmpty()
            }

            override fun afterTextChanged(s: Editable) {}
        }
        editText?.addTextChangedListener(textWatcher)
        btnSend?.setOnClickListener {
            val msg = editText?.text.toString()
            sendMessage(msg)
            editText?.setText("")
            val chatMsg = ChatMsg(UserUtils.userid, toUserId, msg)
            chatViewModel?.sendMessage(chatMsg)
            val message = Message(msg, UserUtils.userid, toUserId, 1)
            chatViewModel?.insertDataBase(message)
        }
        swipe?.setOnRefreshListener {
            toUserId?.let { chatViewModel?.getMoreFive(UserUtils.userid!!, it,page) }
            page++
        }
    }

    fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        recyclerView?.layoutManager = layoutManager
        chatAdapter = ChatAdapter()
        chatAdapter?.setContext(this)
        recyclerView?.adapter = chatAdapter
    }

    /**
     * 发送信息
     *
     * @param message
     */
    private fun sendMessage(message: String) {
        val chatModel = ChatModel(UserUtils.userhead, message, ChatModel.SEND)
        chatAdapter?.addItem(chatModel)
        chatAdapter?.notifyDataSetChanged()
        chatAdapter?.itemCount?.minus(1)?.let { recyclerView?.scrollToPosition(it) }
    }

}