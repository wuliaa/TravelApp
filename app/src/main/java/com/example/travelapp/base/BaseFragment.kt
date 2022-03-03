package com.example.travelapp.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.travelapp.R


open class BaseFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_base, container, false)
    }


    /**
     * 是否拦截fragment的返回事件
     * @return
     */
    fun onBackPressed(): Boolean {
        val iterator: Iterator<Fragment> = childFragmentManager.fragments.iterator()
        while (iterator.hasNext()) {
            val fragment = iterator.next()
            if (fragment is BaseFragment) {
                if (fragment.isResumed() && fragment.onBackPressed()) {
                    return true
                }
            }
        }
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
            return true
        }
        return false
    }

    /**
     * 替换 Fragment
     *
     * @param containerViewId
     * @param fragment
     */
    fun replaceFragment(containerViewId: Int, fragment: Fragment?) {
        val fragmentTransaction: FragmentTransaction = childFragmentManager.beginTransaction()
        if (fragment != null) {
            fragmentTransaction.replace(containerViewId, fragment)
        }
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        fragmentTransaction.commit()
    }

    /**
     * 替换 Fragment
     *
     * @param containerViewId
     * @param fragment
     * @param tag
     */
    fun replaceFragment(containerViewId: Int, fragment: Fragment, tag: String?) {
        if (childFragmentManager.findFragmentByTag(tag) == null) {
            val fragmentTransaction: FragmentTransaction = childFragmentManager.beginTransaction()
            //设置Tag
            fragmentTransaction.replace(containerViewId, fragment, tag)
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            // 这里也要设置tag，通过这里保存的tag找到对应的fragment
            fragmentTransaction.addToBackStack(tag)
            fragmentTransaction.commit()
        } else {
            //存在则弹出在它上面的所有fragment,并显示对应的fragment，类似activity启动模式SingleTack
            childFragmentManager.popBackStackImmediate(tag, 0)
        }
    }
}