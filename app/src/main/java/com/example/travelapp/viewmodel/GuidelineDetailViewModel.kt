package com.example.travelapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONArray
import com.amap.api.services.core.LatLonPoint
import com.example.travelapp.MyApplication
import com.example.travelapp.R
import com.example.travelapp.bean.CommentVo
import com.example.travelapp.bean.ImageViewInfo
import com.example.travelapp.bean.NineGridInfo
import com.example.travelapp.utils.*
import com.example.travelapp.utils.baidu.GsonUtils
import com.xuexiang.xui.widget.imageview.nine.NineGridImageView
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit


class GuidelineDetailViewModel(application: Application) : AndroidViewModel(application) {
    private var sNineGridPics: MutableLiveData<List<NineGridInfo>>? = null
    private var imgs: MutableList<ImageViewInfo>? = null
    private var latLongPoint: SingleLiveEvent<LatLonPoint>? = null
    private var cantNavi: SingleLiveEvent<String>? = null
    private var star: SingleLiveEvent<Boolean>? = null
    private var guidelineId: Long? = null
    private var check: SingleLiveEvent<Boolean>? = null
    private var noNet: SingleLiveEvent<String>? = null
    private var commentList: SingleLiveEvent<List<CommentVo>>? = null
    private var commentSuccess: SingleLiveEvent<CommentVo>? = null
    private var replySuccess: SingleLiveEvent<CommentVo>? = null

    fun getReplySuccess(): SingleLiveEvent<CommentVo>? {
        if (replySuccess == null) replySuccess = SingleLiveEvent()
        return replySuccess
    }
    fun getCommentSuccess(): SingleLiveEvent<CommentVo>? {
        if (commentSuccess == null) commentSuccess = SingleLiveEvent()
        return commentSuccess
    }

    fun getCommentList(): SingleLiveEvent<List<CommentVo>>? {
        if (commentList == null) commentList = SingleLiveEvent()
        return commentList
    }

    fun getNoNet(): SingleLiveEvent<String>? {
        if (noNet == null) noNet = SingleLiveEvent()
        return noNet
    }

    fun httpGetIsStar(guidelineId: Long) {
        star = SingleLiveEvent()
        this.guidelineId = guidelineId
        ThreadPoolUtil.execute {
            val cache =
                Cache(File(MyApplication.getContext()?.cacheDir, "okHttpCache"), 10 * 1024 * 1024)
            val client = OkHttpClient.Builder()
                .addNetworkInterceptor(CacheUtils.NetCacheInterceptor)
                .addInterceptor(CacheUtils.OfflineCacheInterceptor)
                .cache(cache)
                .connectTimeout(1000, TimeUnit.SECONDS) //????????????????????????
                .readTimeout(2000, TimeUnit.SECONDS) //????????????????????????
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                "${
                    getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                }collection/hasCollection?"
            )!!.newBuilder()
            urlBuilder.addQueryParameter("userId", UserUtils.userid)
            urlBuilder.addQueryParameter("guidelineId", guidelineId.toString())
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body()!!.string())
                if (json.getString("code") == "200") {
                    star?.postValue(json.getBoolean("body"))
                }
            }
        }
    }

    fun addCollection() {
        ThreadPoolUtil.execute {
            if (!NetUtils.isConnected(getApplication())) {
                getNoNet()?.postValue("?????????????????????")
                return@execute
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS) //????????????????????????
                .readTimeout(2000, TimeUnit.SECONDS) //????????????????????????
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                "${
                    getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                }collection/addCollection?"
            )!!.newBuilder()
            urlBuilder.addQueryParameter("userId", UserUtils.userid)
            urlBuilder.addQueryParameter("guidelineId", guidelineId.toString())
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                check?.postValue(true)
            }
        }
    }

    fun deleteCollection() {
        ThreadPoolUtil.execute {
            if (!NetUtils.isConnected(getApplication())) {
                getNoNet()?.postValue("?????????????????????")
                return@execute
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS) //????????????????????????
                .readTimeout(2000, TimeUnit.SECONDS) //????????????????????????
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                "${
                    getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                }collection/deleteCollection?"
            )!!.newBuilder()
            urlBuilder.addQueryParameter("userId", UserUtils.userid)
            urlBuilder.addQueryParameter("guidelineId", guidelineId.toString())
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                check?.postValue(false)
            }
        }
    }

    fun getComments() {
        ThreadPoolUtil.execute {
            val client = OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS) //????????????????????????
                .readTimeout(2000, TimeUnit.SECONDS) //????????????????????????
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                "${
                    getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                }comment/queryComment?"
            )!!.newBuilder()
            urlBuilder.addQueryParameter("guidelineId", guidelineId.toString())
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val array: JSONArray = JSONArray.parseArray(response.body()!!.string())
                val list: List<CommentVo> = com.alibaba.fastjson.JSONObject.parseArray(
                    array.toJSONString(),
                    CommentVo::class.java
                )
                commentList?.postValue(list)
            }
        }
    }

    fun addComment(content: String) {
        ThreadPoolUtil.execute {
            if (!NetUtils.isConnected(getApplication())) {
                getNoNet()?.postValue("?????????????????????")
                return@execute
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS) //????????????????????????
                .readTimeout(2000, TimeUnit.SECONDS) //????????????????????????
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                "${
                    getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                }comment/addComment?"
            )!!.newBuilder()
            urlBuilder.addQueryParameter("userId", UserUtils.userid)
            urlBuilder.addQueryParameter("guidelineId", guidelineId.toString())
            urlBuilder.addQueryParameter("content", content)
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val commentVo =
                    GsonUtils.fromJson(response.body()!!.string(), CommentVo::class.java)
                getCommentSuccess()?.postValue(commentVo)
            }
        }
    }

    fun addReply(content: String, parentId: Long, parentNickname: String) {
        ThreadPoolUtil.execute {
            if (!NetUtils.isConnected(getApplication())) {
                getNoNet()?.postValue("?????????????????????")
                return@execute
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS) //????????????????????????
                .readTimeout(2000, TimeUnit.SECONDS) //????????????????????????
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                "${
                    getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                }comment/addComment?"
            )!!.newBuilder()
            urlBuilder.addQueryParameter("userId", UserUtils.userid)
            urlBuilder.addQueryParameter("guidelineId", guidelineId.toString())
            urlBuilder.addQueryParameter("content", content)
            urlBuilder.addQueryParameter("parentId", parentId.toString())
            urlBuilder.addQueryParameter("parentNickname", parentNickname)
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val commentVo =
                    GsonUtils.fromJson(response.body()!!.string(), CommentVo::class.java)
                getCommentSuccess()?.postValue(commentVo)
            }
        }
    }

    fun addReply2(content: String, parentId: Long, parentNickname: String) {
        ThreadPoolUtil.execute {
            if (!NetUtils.isConnected(getApplication())) {
                getNoNet()?.postValue("?????????????????????")
                return@execute
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS) //????????????????????????
                .readTimeout(2000, TimeUnit.SECONDS) //????????????????????????
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                "${
                    getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                }comment/addComment?"
            )!!.newBuilder()
            urlBuilder.addQueryParameter("userId", UserUtils.userid)
            urlBuilder.addQueryParameter("guidelineId", guidelineId.toString())
            urlBuilder.addQueryParameter("content", content)
            urlBuilder.addQueryParameter("parentId", parentId.toString())
            urlBuilder.addQueryParameter("parentNickname", parentNickname)
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val commentVo =
                    GsonUtils.fromJson(response.body()!!.string(), CommentVo::class.java)
                getReplySuccess()?.postValue(commentVo)
            }
        }
    }


    val list = mutableListOf<CommentVo>()
    fun initCommentList(ans: List<CommentVo>) {
        list.clear()
        circle(ans)
        list.sortBy { it.createTime }
    }

    fun circle(ans: List<CommentVo>) {
        for (i in ans) {
            list.add(i)
            if (i.child != null) {
                circle(i.child)
            }
            i.child = null
        }
    }

    fun getStar(): SingleLiveEvent<Boolean>? {
        if (star == null) star = SingleLiveEvent()
        return star
    }

    fun getCheck(): SingleLiveEvent<Boolean>? {
        if (check == null) check = SingleLiveEvent()
        return check
    }

    fun getCantNavi(): SingleLiveEvent<String>? {
        if (cantNavi == null) cantNavi = SingleLiveEvent()
        return cantNavi
    }

    fun setCantNavi(s: String) {
        if (cantNavi == null) cantNavi = SingleLiveEvent()
        cantNavi?.value = s
    }

    fun getPics(): MutableLiveData<List<NineGridInfo>>? {
        if (sNineGridPics == null) sNineGridPics = MutableLiveData()
        return sNineGridPics
    }

    fun setLatLonPoint(lat: LatLonPoint) {
        if (latLongPoint == null) latLongPoint = SingleLiveEvent()
        latLongPoint?.value = lat
    }

    fun getLatLonPoint(): SingleLiveEvent<LatLonPoint>? {
        if (latLongPoint == null) latLongPoint = SingleLiveEvent()
        return latLongPoint
    }

    fun setImgs(photo: String) {
        ThreadPoolUtil.execute {
            imgs = arrayListOf()
            val list: List<String> = getUrls(photo)
            for (i in list.indices) {
                imgs?.add(ImageViewInfo(list[i]))
            }
            sNineGridPics?.postValue(getMediaDemos())
        }
    }

    private fun getUrls(photo: String): List<String> {
        val urls: MutableList<String> = ArrayList()
        val s = photo.split(",")
        for (i in s) {
            urls.add(
                "${
                    MyApplication.getContext()?.getString(R.string.serverBasePath)
                }images/${i}"
            )
        }
        return urls
    }

    private fun getMediaDemos(): List<NineGridInfo> {
        val list: MutableList<NineGridInfo> = ArrayList()
        val info: NineGridInfo = NineGridInfo("", imgs).setShowType(NineGridImageView.STYLE_FILL)
        list.add(info)
        return list
    }

}