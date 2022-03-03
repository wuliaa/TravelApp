package com.example.travelapp.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.travelapp.R
import com.example.travelapp.utils.BaikeUtil
import com.example.travelapp.utils.NetUtils
import com.example.travelapp.utils.ThreadPoolUtil
import com.example.travelapp.utils.ali.AliLandmark
import com.example.travelapp.utils.baidu.BaiduLandmark
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class RecognitionViewModel(application: Application) : AndroidViewModel(application) {
    private var result: MutableLiveData<String>? = null
    private var bitmap: SingleLiveEvent<Bitmap>? = null
    private var introduction: MutableLiveData<String>? = null
    private var noNet: SingleLiveEvent<String>? = null

    fun getNoNet(): SingleLiveEvent<String>? {
        if (noNet == null) noNet = SingleLiveEvent()
        return noNet
    }

    fun getResult(): MutableLiveData<String>? {
        if (result == null) {
            result = MutableLiveData()
        }
        return result
    }

    fun getIntroduction(): MutableLiveData<String>? {
        if (introduction == null) introduction = MutableLiveData()
        return introduction
    }

    fun setBitmap(bm: Bitmap) {
        bitmap = SingleLiveEvent<Bitmap>()
        bitmap?.value = bm
    }

    fun getBitmap(): SingleLiveEvent<Bitmap>? {
        if (bitmap == null) bitmap = SingleLiveEvent()
        return bitmap
    }

    fun baiduLandmark(filePath: String) {
        ThreadPoolUtil.execute {
            if (!NetUtils.isConnected(getApplication())) {
                getNoNet()?.postValue("请检查网络连接")
                return@execute
            }
            val s = BaiduLandmark.landmark(filePath)
            result?.postValue(s)
        }
    }

    fun aliLandmark(filePath: String) {
        ThreadPoolUtil.execute {
            val base64 = AliLandmark.landmark(filePath)
            val client = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS) //设置连接超时时间
                .readTimeout(20, TimeUnit.SECONDS) //设置读取超时时间
                .build()
            val reqBuilder: Request.Builder = Request.Builder()
            val urlBuilder = HttpUrl.parse(
                getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                        + "test/alitest"
            )!!.newBuilder()
            val body = FormBody.Builder().add("base64", base64).build()
            reqBuilder.url(urlBuilder.build())
            val request: Request = reqBuilder.post(body).build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body()!!.string())
                Log.e("111", json.toString())

            }
        }
    }

    fun crawl(name: String) {
        ThreadPoolUtil.execute {
            val s = BaikeUtil.queryBaike(name)
            if (s.isEmpty()) {
                getNoNet()?.postValue("查询不到相关简介")
            } else {
                var ans = "        "
                for (i in s) {
                    ans += "$i\n        "
                }
                introduction?.postValue(ans)
            }
        }
    }

    fun addRecognition(
        userId: String, filePath: String, landmarkName: String,
        landmarkDescribe: String
    ) {
        ThreadPoolUtil.execute {
            val client = OkHttpClient.Builder()
            val reqBuilder: MultipartBody.Builder =
                MultipartBody.Builder().setType(MultipartBody.FORM)
            val file = File(filePath)
            val fileBody = RequestBody.create(MediaType.parse("image/*"), file)
            reqBuilder.addFormDataPart("file", file.name, fileBody)
            val request = Request.Builder()
                .url(
                    "${
                        getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                    }recognition/upload?"
                )
                .post(reqBuilder.build())
                .build()
            val response = client.readTimeout(5000, TimeUnit.MILLISECONDS)
                .build()
                .newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body()!!.string())
                val filesUrl = json.getString("body")
                val client2 = OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS) //设置连接超时时间
                    .readTimeout(20, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
                val reqBuilder2: Request.Builder = Request.Builder()
                val urlBuilder = HttpUrl.parse(
                    "${
                        getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                    }recognition/addRecognition"
                )!!.newBuilder()
                urlBuilder.addQueryParameter("userId", userId)
                urlBuilder.addQueryParameter("photo", filesUrl)
                urlBuilder.addQueryParameter("landmarkName", landmarkName)
                urlBuilder.addQueryParameter("landmarkDescribe", landmarkDescribe)
                reqBuilder2.url(urlBuilder.build())
                val request2: Request = reqBuilder2.build()
                val response2: Response = client2.newCall(request2).execute()
            }
        }
    }
}