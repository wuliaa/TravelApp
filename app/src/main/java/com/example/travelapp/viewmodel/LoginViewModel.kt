package com.example.travelapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.travelapp.R
import com.example.travelapp.utils.OkHttpUtils
import com.example.travelapp.utils.ThreadPoolUtil
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.lang.Exception
import java.util.concurrent.TimeUnit


class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private var result: SingleLiveEvent<JSONObject>? = null
    private var wrong: SingleLiveEvent<String>? = null
    fun getResult(): SingleLiveEvent<JSONObject>? {
        if (result == null) {
            result = SingleLiveEvent()
        }
        return result
    }

    fun getWrong(): SingleLiveEvent<String>? {
        if (wrong == null) {
            wrong = SingleLiveEvent()
        }
        return wrong
    }

    fun login(username: String, password: String) {
        val params: List<OkHttpUtils.Param> = listOf(
            OkHttpUtils.Param("nickname", username),
            OkHttpUtils.Param("passwd", password)
        )
        OkHttpUtils.post(
            "${
                getApplication<Application>().applicationContext.getString(R.string.serverBasePath)
            }user/login?", object : OkHttpUtils.ResultCallback<String>() {
                override fun onSuccess(response: String) {
                    val json = JSONObject(response)
                    result?.postValue(json)
                }
                override fun onFailure(e: Exception) {
                    getWrong()?.value = "服务器错误"
                }
            }, params
        )
    }
}