package com.example.travelapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.travelapp.MyApplication
import com.example.travelapp.R
import com.example.travelapp.bean.ChatModel
import com.example.travelapp.bean.ChatMsg
import com.example.travelapp.bean.Message
import com.example.travelapp.database.ChatDao
import com.example.travelapp.database.ChatDataBase
import com.example.travelapp.utils.NetUtils
import com.example.travelapp.utils.ThreadPoolUtil
import com.example.travelapp.utils.UserUtils
import com.example.travelapp.utils.WebSocketUtils
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit


class ChatViewModel(application: Application) :AndroidViewModel(application) {

    private var chatDataBase: ChatDataBase = ChatDataBase.getDatabase(application)
    private var chatDao: ChatDao = chatDataBase.chatDao
    var toUserPortrait: String? = null
    private var clist:SingleLiveEvent<MutableList<ChatModel>>?=null
    private var morelist:SingleLiveEvent<MutableList<ChatModel>>?=null
    private var noNet:SingleLiveEvent<String>?=null

    fun getNoNet():SingleLiveEvent<String>?{
        if(noNet == null)noNet = SingleLiveEvent()
        return noNet
    }
    fun getCList():SingleLiveEvent<MutableList<ChatModel>>?{
        if(clist == null)clist = SingleLiveEvent()
        return clist
    }
    fun getMoreList():SingleLiveEvent<MutableList<ChatModel>>?{
        if(morelist == null)morelist = SingleLiveEvent()
        return morelist
    }

    fun getLastFive(fromUser:String,toUser:String):LiveData<List<Message>>?{
        return chatDao.getLastFive(fromUser,toUser)
    }

    fun getMoreFive(fromUser:String,toUser:String,page:Int){
        ThreadPoolUtil.execute {
            val list = chatDao.getMoreFive(fromUser,toUser,page).asReversed()
            if(list.isEmpty()){
                getNoNet()?.postValue("已无更多聊天记录")
                return@execute
            }
            val chatList : MutableList<ChatModel> = mutableListOf()
            for(i in list){
                if(i.from_user==UserUtils.userid)
                    chatList.add(ChatModel(UserUtils.userhead,i.content,ChatModel.SEND))
                else chatList.add(ChatModel(toUserPortrait,i.content,ChatModel.RECEIVE))
            }
            getMoreList()?.postValue(chatList)
        }
    }

    fun initData(list:List<Message>){
        ThreadPoolUtil.execute {
            val chatList : MutableList<ChatModel> = mutableListOf()
            for(i in list){
                if(i.from_user==UserUtils.userid)
                    chatList.add(ChatModel(UserUtils.userhead,i.content,ChatModel.SEND))
                else chatList.add(ChatModel(toUserPortrait,i.content,ChatModel.RECEIVE))
            }
            getCList()?.postValue(chatList)
        }
    }


    fun sendMessage(chatMsg: ChatMsg){
        WebSocketUtils.getInstance().sendData(chatMsg)
    }

    fun changeMsgRead(msgIds: String){
        ThreadPoolUtil.execute {
            if (!NetUtils.isConnected(getApplication())) {
                getNoNet()?.postValue("请检查网络连接")
                return@execute
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS) //设置连接超时时间
                .readTimeout(2000, TimeUnit.SECONDS) //设置读取超时时间
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                "${
                    getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                }dialog/read?"
            )!!.newBuilder()
            urlBuilder.addQueryParameter("msgIds", msgIds)
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.build()
            client.newCall(request).execute()
        }
    }

    fun insertDataBase(message: Message){
        ThreadPoolUtil.execute {
            ChatDataBase.getDatabase(getApplication()).chatDao.insert(message)
        }
    }

}