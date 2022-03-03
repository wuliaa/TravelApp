package com.example.travelapp.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import com.example.travelapp.R


open class BaseActivity : FragmentActivity() {
    private var root_layout: LinearLayout? = null
    private var toolbar_layout: View? = null
    private var left_back: TextView? = null
    private var title: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 重点
        super.setContentView(R.layout.activity_base)
        initToolbar()
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        setContentView(View.inflate(this, layoutResID, null))
    }

    override fun setContentView(view: View) {
        root_layout = findViewById<View>(R.id.root_layout) as LinearLayout
        root_layout?.addView(
            view, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        initToolbar()
    }

    /**
     * 初始化 toolbar 内容布局
     */
    private fun initToolbar() {
        left_back = f<TextView>(R.id.tv_back)
        title = f<TextView>(R.id.tv_title)
        toolbar_layout = f<View>(R.id.ll_toolbar)
    }

    /**
     * 设置返回按钮
     */
    protected fun setBack() {
        left_back?.visibility = View.VISIBLE
        left_back?.setOnClickListener { onBackPressed() }
    }

    /**
     * 设置当前 Activity 标题
     *
     * @param title
     */
    protected fun setTitle(mtitle: String?) {
        title?.visibility = View.VISIBLE
        title?.text = mtitle
    }

    /**
     * 隐藏头部标题栏
     */
    protected fun hideToolbar() {
        toolbar_layout?.visibility = View.GONE
    }

    protected fun <T : View?> f(id: Int): T {
        return findViewById<View>(id) as T
    }
}
