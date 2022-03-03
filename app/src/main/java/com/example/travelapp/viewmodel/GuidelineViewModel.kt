package com.example.travelapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.travelapp.R
import com.example.travelapp.utils.ThreadPoolUtil
import com.luck.picture.lib.entity.LocalMedia
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class GuidelineViewModel(application: Application) :AndroidViewModel(application) {

    private val files :MutableList<File> = ArrayList()
    private var result: MutableLiveData<JSONObject>? = null

    fun getResult(): MutableLiveData<JSONObject>? {
        if (result == null) {
            result = MutableLiveData()
        }
        return result
    }

    fun setFiles(list:List<LocalMedia>){
        for(i in list){
            files.add(File(i.path))
        }
    }

    fun postGuideline(userId:String,title:String,content:String,city:String,landmarkName:String){
        ThreadPoolUtil.execute {
            val client = OkHttpClient.Builder()
            val reqBuilder: MultipartBody.Builder =
                MultipartBody.Builder().setType(MultipartBody.FORM)
            for (file in files) {
                val fileBody = RequestBody.create(MediaType.parse("image/*"), file)
                reqBuilder.addFormDataPart("files", file.name, fileBody)
            }
            val request = Request.Builder()
                .url("${getApplication<Application>().
                applicationContext.getString(R.string.serverBasePath)}guideline/upload?")
                .post(reqBuilder.build())
                .build()
            val response = client.readTimeout(5000, TimeUnit.MILLISECONDS)
                .build()
                .newCall(request).execute()
            if(response.isSuccessful){
                val json = JSONObject(response.body()!!.string())
                if (json.getString("code") != "200") {
                    result?.postValue(json)
                }else{
                    val filesUrl= json.getString("body")
                    val client2 = OkHttpClient.Builder()
                        .connectTimeout(100, TimeUnit.SECONDS) //设置连接超时时间
                        .readTimeout(20, TimeUnit.SECONDS) //设置读取超时时间
                        .build()
                    val reqBuilder2: Request.Builder = Request.Builder()
                    val urlBuilder = HttpUrl.parse("${getApplication<Application>().
                    applicationContext.getString(R.string.serverBasePath)}guideline/postGuideline?"
                    )!!.newBuilder()
                    urlBuilder.addQueryParameter("userId", userId)
                    urlBuilder.addQueryParameter("title", title)
                    urlBuilder.addQueryParameter("photo",filesUrl)
                    val address = "${city}市 $landmarkName"
                    urlBuilder.addQueryParameter("landmarkName",address)
                    urlBuilder.addQueryParameter("content",content)
                    reqBuilder2.url(urlBuilder.build())
                    val request2: Request = reqBuilder2.build()
                    val response2: Response = client2.newCall(request2).execute()
                    if(response2.isSuccessful){
                        val json2 = JSONObject(response2.body()!!.string())
                        result?.postValue(json2)
                    }
                }
            }
        }
    }
}