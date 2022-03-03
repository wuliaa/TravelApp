package com.example.travelapp.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.travelapp.MainActivity
import com.example.travelapp.R
import com.example.travelapp.bean.UserVo
import com.example.travelapp.utils.NetUtils
import com.example.travelapp.utils.UserUtils
import com.example.travelapp.utils.XToastUtils
import com.example.travelapp.viewmodel.LoginViewModel
import com.google.gson.Gson
import com.xuexiang.xui.utils.KeyboardUtils
import com.xuexiang.xui.utils.StatusBarUtils
import com.xuexiang.xutil.app.ActivityUtils
import com.xuexiang.xutil.display.Colors
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.et_pass_word
import kotlinx.android.synthetic.main.activity_login.et_user_name
import kotlinx.android.synthetic.main.activity_login.progressbar
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private var loginViewModel: LoginViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtils.initStatusBarStyle(this, false, Colors.WHITE)
        setContentView(R.layout.activity_login)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        initView()
        loginResult()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return KeyboardUtils.onDisableBackKeyDown(1) && super.onKeyDown(keyCode, event)
    }

    private fun initView() {
        btn_login.isEnabled = false
        progressbar.visibility = View.INVISIBLE
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val t1 = et_user_name.text.toString().isNotEmpty()
                val t2 = et_pass_word.text.toString().isNotEmpty()
                btn_login.isEnabled = t1 and t2
            }

            override fun afterTextChanged(s: Editable) {}
        }
        et_user_name.addTextChangedListener(textWatcher)
        et_pass_word.addTextChangedListener(textWatcher)
        btn_login.setOnClickListener {
            if (!NetUtils.isConnected(applicationContext)) {
                XToastUtils.error("无网络连接")
            } else {
                loginViewModel?.login(
                    et_user_name.text.toString(),
                    et_pass_word.text.toString()
                )
                progressbar.visibility = View.VISIBLE
            }
        }
        tv_go_register.setOnClickListener {
            ActivityUtils.startActivity(RegisterActivity::class.java)
        }
    }

    private fun loginResult() {
        loginViewModel?.getResult()?.observe(this, {
            progressbar.visibility = View.INVISIBLE
            if (it.getString("code") == "200") {
                XToastUtils.success("登录成功")
                spOperation(it)
                ActivityUtils.startActivity(MainActivity::class.java)
                finish()
            } else {
                XToastUtils.error(it.getString("message"))
            }
        })
        loginViewModel?.getWrong()?.observe(this, {
            progressbar.visibility = View.INVISIBLE
            XToastUtils.error(it)
        })
    }

    private fun spOperation(body: JSONObject) {
        UserUtils.username = et_user_name.text.toString()
        UserUtils.password = et_pass_word.text.toString()
        val str = body.getJSONObject("body").toString()
        val userVo: UserVo = Gson().fromJson(str, UserVo::class.java)
        UserUtils.userid = userVo.userId
        UserUtils.userhead = userVo.userHead
    }
}