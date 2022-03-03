package com.example.travelapp.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.travelapp.R
import com.example.travelapp.fragment.MyGuidelineDetailFragment
import com.example.travelapp.fragment.MyGuidelineFragment
import com.example.travelapp.viewmodel.MyGuidelineViewModel

class MyGuidelineActivity : AppCompatActivity() {
    private var myGuidelineViewModel:MyGuidelineViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_guideline)
        myGuidelineViewModel = ViewModelProvider(this).get(MyGuidelineViewModel::class.java)
        showFragment(MyGuidelineFragment())
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        supportFragmentManager.fragments.last()?.let{
            if(it is MyGuidelineDetailFragment){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MyGuidelineFragment())
                    .commit()
            }else{
                finish()
            }
        }
    }

    //region 点击隐藏键盘
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (isHideInput(view, ev)) {
                HideSoftInput(view!!.windowToken)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    // 判定是否需要隐藏
    private fun isHideInput(v: View?, ev: MotionEvent): Boolean {
        if (v != null && v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = (left
                    + v.getWidth())
            return !(ev.x > left && ev.x < right && ev.y > top && ev.y < bottom)
        }
        return false
    }

    // 隐藏软键盘
    private fun HideSoftInput(token: IBinder?) {
        if (token != null) {
            val manager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}