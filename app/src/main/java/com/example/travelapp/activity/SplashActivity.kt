package com.example.travelapp.activity

import android.view.KeyEvent
import android.view.WindowManager
import com.example.travelapp.MainActivity
import com.example.travelapp.utils.ThreadPoolUtil
import com.example.travelapp.utils.UserUtils
import com.example.travelapp.utils.WebSocketUtils
import com.xuexiang.xui.utils.KeyboardUtils
import com.xuexiang.xui.widget.activity.BaseSplashActivity
import com.xuexiang.xutil.app.ActivityUtils
import me.jessyan.autosize.internal.CancelAdapt

class SplashActivity : BaseSplashActivity(), CancelAdapt {

    override fun getSplashDurationMillis(): Long {
        return 800
    }

    override fun onCreateActivity() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        startSplash(false)
    }

    override fun onSplashFinished() {
        loginOrGoMainPage()
    }

    private fun loginOrGoMainPage() {
        if (UserUtils.hasUser()) {
            ThreadPoolUtil.execute {
                WebSocketUtils.getInstance().initSocket()
            }
            ActivityUtils.startActivity(MainActivity::class.java)
        } else {
            ActivityUtils.startActivity(LoginActivity::class.java)
        }
        finish()
    }

    /**
     * 菜单、返回键响应
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return KeyboardUtils.onDisableBackKeyDown(keyCode) && super.onKeyDown(keyCode, event)
    }

}