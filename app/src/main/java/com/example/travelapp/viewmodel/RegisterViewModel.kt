package com.example.travelapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.travelapp.R
import com.example.travelapp.utils.ThreadPoolUtil
import com.example.travelapp.utils.UserUtils
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private var result: MutableLiveData<JSONObject>? = null
    fun getResult(): MutableLiveData<JSONObject>? {
        if (result == null) {
            result = MutableLiveData()
        }
        return result
    }

    fun register(username: String, password: String, file: File) {
        ThreadPoolUtil.execute {
            val client = OkHttpClient.Builder()
            val reqBuilder: MultipartBody.Builder =
                MultipartBody.Builder().setType(MultipartBody.FORM)
            val fileBody = RequestBody.create(MediaType.parse("image/*"), file)
            reqBuilder.addFormDataPart("file", file.name, fileBody)
            val request = Request.Builder()
                .url(getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                            + "user/upload?")
                .post(reqBuilder.build())
                .build()
            val response = client.readTimeout(5000, TimeUnit.MILLISECONDS)
                .build()
                .newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body()!!.string())
                if (json.getString("code") != "200") {
                    result?.postValue(json)
                } else {
                    val imageUrl = json.getString("body")
                    UserUtils.userhead = imageUrl
                    val client2 = OkHttpClient.Builder()
                        .connectTimeout(100, TimeUnit.SECONDS) //设置连接超时时间
                        .readTimeout(20, TimeUnit.SECONDS) //设置读取超时时间
                        .build()
                    val reqBuilder2: Request.Builder = Request.Builder()
                    val urlBuilder = HttpUrl.parse(
                        getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
                                + "user/register?"
                    )!!.newBuilder()
                    urlBuilder.addQueryParameter("nickname", username)
                    urlBuilder.addQueryParameter("passwd", password)
                    urlBuilder.addQueryParameter("userHead",imageUrl)
                    reqBuilder2.url(urlBuilder.build())
                    val request2: Request = reqBuilder2.build()
                    val response2: Response = client2.newCall(request2).execute()
                    if (response2.isSuccessful) {
                        val json2 = JSONObject(response2.body()!!.string())
                        Log.e("111",json2.toString())
                        result?.postValue(json2)
                    }
                }
            }
        }
    }
}