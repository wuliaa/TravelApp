package com.example.travelapp.utils

import android.content.Context
import com.example.travelapp.activity.LoginActivity
import com.xuexiang.xutil.app.ActivityUtils
import com.xuexiang.xutil.common.StringUtils

class UserUtils private constructor() {
    companion object {
        private var sUserId:String?=null
        private var sUserName: String? = null
        private var sPasswd: String? = null
        private var sUserHead:String? = null
        private const val KEY_USERID = "KEY_USERID"
        private const val KEY_USERNAME = "KEY_USERNAME"
        private const val KEY_PASSWORD = "KEY_PASSWORD"
        private const val KEY_USERHEAD = "KET_USERHEAD"

        /**
         * 初始化user信息
         */
        fun init(context: Context?) {
            MMKVUtils.init(context)
            sUserId = MMKVUtils.getString(KEY_USERID,"")
            sUserName = MMKVUtils.getString(KEY_USERNAME, "")
            sPasswd = MMKVUtils.getString(KEY_PASSWORD,"")
            sUserHead = MMKVUtils.getString(KEY_USERHEAD,"")
        }

        fun clearUser() {
            sUserId = null
            sUserName = null
            sPasswd = null
            sUserHead = null
            MMKVUtils.remove(KEY_USERID)
            MMKVUtils.remove(KEY_USERNAME)
            MMKVUtils.remove(KEY_PASSWORD)
            MMKVUtils.remove(KEY_USERHEAD)
        }

        var userid:String?
            get() = sUserId
            set(userid){
                sUserId = userid
                MMKVUtils.put(KEY_USERID,userid)
            }

        var username: String?
            get() = sUserName
            set(username) {
                sUserName = username
                MMKVUtils.put(KEY_USERNAME, username)
            }

        var password: String?
            get() = sPasswd
            set(password) {
                sPasswd = password
                MMKVUtils.put(KEY_PASSWORD, password)
            }

        var userhead:String?
            get() = sUserHead
            set(userhead){
                sUserHead = userhead
                MMKVUtils.put(KEY_USERHEAD,userhead)
            }
        fun hasUser(): Boolean {
            return MMKVUtils.containsKey(KEY_USERNAME)&&MMKVUtils.containsKey(KEY_PASSWORD)
        }


        /**
         * 处理登出的事件
         */
        fun handleLogoutSuccess() {
            //登出时，清除账号信息
            clearUser()
            XToastUtils.success("登出成功！")
            //跳转到登录页
            ActivityUtils.startActivity(LoginActivity::class.java)
        }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}