package com.example.travelapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.example.travelapp.MyApplication
import com.example.travelapp.R
import com.example.travelapp.bean.DialogVo
import com.example.travelapp.bean.Recognition
import com.example.travelapp.utils.CacheUtils
import com.example.travelapp.utils.ThreadPoolUtil
import com.example.travelapp.utils.UserUtils
import okhttp3.*
import java.io.File
import java.util.concurrent.TimeUnit

class MyDialogViewModel(application: Application) :AndroidViewModel(application) {
    private var dialogList: SingleLiveEvent<List<DialogVo>>? = null

    fun getList(): SingleLiveEvent<List<DialogVo>>? {
        if (dialogList == null) dialogList = SingleLiveEvent()
        return dialogList
    }

    fun asyncData() {
        ThreadPoolUtil.execute {
            val cache = Cache(File(MyApplication.getContext()?.cacheDir, "okHttpCache"), 10 * 1024 * 1024)
            val client = OkHttpClient.Builder()
                .addNetworkInterceptor(CacheUtils.NetCacheInterceptor)
                .addInterceptor(CacheUtils.OfflineCacheInterceptor)
                .cache(cache)
                .connectTimeout(1000, TimeUnit.SECONDS) //设置连接超时时间
                .readTimeout(2000, TimeUnit.SECONDS) //设置读取超时时间
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                "${
                    getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                }dialog/queryTheLast?"
            )!!.newBuilder()
            urlBuilder.addQueryParameter("fromUserId", UserUtils.userid)
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val array: JSONArray = JSONArray.parseArray(response.body()!!.string())
                val list: List<DialogVo> = JSONObject.parseArray(
                    array.toJSONString(),
                    DialogVo::class.java
                )
                dialogList?.postValue(list)
            }
        }
    }

    fun refresh() {
        ThreadPoolUtil.execute {
            val client = OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS) //设置连接超时时间
                .readTimeout(2000, TimeUnit.SECONDS) //设置读取超时时间
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                "${
                    getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                }dialog/queryTheLast?"
            )!!.newBuilder()
            urlBuilder.addQueryParameter("fromUserId", UserUtils.userid)
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val array: JSONArray = JSONArray.parseArray(response.body()!!.string())
                val list: List<DialogVo> = JSONObject.parseArray(
                    array.toJSONString(),
                    DialogVo::class.java
                )
                dialogList?.postValue(list)
            }
        }
    }

}