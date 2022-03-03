package com.example.travelapp

import android.app.Application
import android.content.Context
import com.bumptech.glide.Glide
import com.example.travelapp.utils.UserUtils




class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        UserUtils.init(this)
        context = applicationContext
    }
    companion object{
        private var context:Context?=null
        fun getContext():Context?{
            return context
        }
    }

    /**
     * 在 lowMemory 的时候，调用 Glide.cleanMemroy() 清理掉所有的内存缓存。
     * 在 App 被置换到后台的时候，调用 Glide.cleanMemroy() 清理掉所有的内存缓存。
     *在其它情况的 onTrimMemroy() 回调中，直接调用 Glide.trimMemory()
     * 来交给 Glide 处理内存情况。
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if(level == TRIM_MEMORY_UI_HIDDEN){
            Glide.get(this).clearMemory()
        }
        Glide.get(this).trimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.get(this).clearMemory()
    }
}