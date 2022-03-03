package com.example.travelapp.customview

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.ExpandableListView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper


/**
 * Desc: 自定义ExpandableListView,解决与CoordinatorLayout滑动冲突问题
 */
class CommentExpandableListView(context: Context?, attrs: AttributeSet?) :
    ExpandableListView(context, attrs), NestedScrollingChild {
    private val mScrollingChildHelper: NestedScrollingChildHelper = NestedScrollingChildHelper(this)
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mScrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, offsetInWindow: IntArray?
    ): Boolean {
        return mScrollingChildHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed, offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return mScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return mScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isNestedScrollingEnabled = true
        }
    }
}