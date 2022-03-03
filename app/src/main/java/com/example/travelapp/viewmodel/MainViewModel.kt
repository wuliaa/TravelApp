package com.example.travelapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.example.travelapp.R
import com.example.travelapp.bean.DialogVo
import com.example.travelapp.bean.GuidelineVo
import com.example.travelapp.utils.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.lang.Exception
import java.util.concurrent.TimeUnit


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var guidelineList: SingleLiveEvent<List<GuidelineVo>>? = null
    private var guidelineMore: SingleLiveEvent<List<GuidelineVo>>? = null
    private var noNet: SingleLiveEvent<String>? = null
    private var unRead: SingleLiveEvent<Boolean>? = null

    fun getList(): SingleLiveEvent<List<GuidelineVo>>? {
        if (guidelineList == null) {
            guidelineList = SingleLiveEvent()
        }
        return guidelineList
    }

    fun getMoreList(): SingleLiveEvent<List<GuidelineVo>>? {
        if (guidelineMore == null) {
            guidelineMore = SingleLiveEvent()
        }
        return guidelineMore
    }

    fun getNoNet(): SingleLiveEvent<String>? {
        if (noNet == null) noNet = SingleLiveEvent()
        return noNet
    }

    fun getUnread(): SingleLiveEvent<Boolean>? {
        if (unRead == null) unRead = SingleLiveEvent()
        return unRead
    }

    fun asyncData(page:Int) {
        val params :List<OkHttpUtils.Param> = listOf(
            OkHttpUtils.Param("page", page.toString()))
        OkHttpUtils.post("${
            getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
        }guideline/getGuidelineByPage?",object :OkHttpUtils.ResultCallback<String>(){
            override fun onSuccess(response: String) {
                val array: JSONArray = JSONArray.parseArray(response)
                val list: List<GuidelineVo> = JSONObject.parseArray(
                    array.toJSONString(),
                    GuidelineVo::class.java
                )
                guidelineMore?.postValue(list)
            }
            override fun onFailure(e: Exception) {
                getNoNet()?.postValue("请检查网络连接")
            }
        },params)
    }

    fun query(query: String) {
        val params :List<OkHttpUtils.Param> = listOf(
            OkHttpUtils.Param("landmarkName", query))
        OkHttpUtils.post("${
            getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
        }guideline/getAllByLandmarkName?",object :OkHttpUtils.ResultCallback<String>(){
            override fun onSuccess(response: String) {
                val array: JSONArray = JSONArray.parseArray(response)
                val list: List<GuidelineVo> = JSONObject.parseArray(
                    array.toJSONString(),
                    GuidelineVo::class.java
                )
                guidelineList?.postValue(list)
            }
            override fun onFailure(e: Exception) {
                getNoNet()?.postValue("服务器错误")
            }
        },params)
    }

    fun unRead() {
        ThreadPoolUtil.execute {
            val client = OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS) //设置连接超时时间
                .readTimeout(2000, TimeUnit.SECONDS) //设置读取超时时间
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                "${
                    getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                }dialog/queryUnread?"
            )!!.newBuilder()
            urlBuilder.addQueryParameter("fromUserId", UserUtils.userid)
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val ans = response.body()!!.string().toBoolean()
                getUnread()?.postValue(ans)
            }
        }
    }

    /**
     * webSocket open
     */
    fun webSocketOpen(){
        ThreadPoolUtil.execute {
            WebSocketUtils.getInstance().initSocket()
        }
    }
}