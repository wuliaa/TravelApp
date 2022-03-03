package com.example.travelapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.travelapp.R
import com.example.travelapp.adapter.GuidelineAdapter
import com.example.travelapp.adapter.MyItemDecoration
import com.example.travelapp.base.BaseFragment
import com.example.travelapp.viewmodel.MyCollectionViewModel
import kotlinx.android.synthetic.main.layout_toolbar.*

class MyCollectionFragment : BaseFragment() {
    private var myCollectionViewModel:MyCollectionViewModel?=null
    private var recyclerView: RecyclerView? = null
    private var adapter: GuidelineAdapter? = null
    private var progressBar: ProgressBar?=null
    private var swip: SwipeRefreshLayout?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myCollectionViewModel = ViewModelProvider(requireActivity()).get(MyCollectionViewModel::class.java)
        initViews()
    }

    private fun initViews(){
        setBack()
        setTitle("我的收藏")
        initRecyclerView()
        progressBar = requireActivity().findViewById(R.id.my_collection_progressbar)
        progressBar?.visibility = View.VISIBLE
        swip = requireActivity().findViewById(R.id.swipe_collection)
        swip?.setOnRefreshListener {
            myCollectionViewModel?.refresh()
        }
    }

    private fun initRecyclerView(){
        myCollectionViewModel?.asyncData()
        //适配器布局
        recyclerView = requireActivity().findViewById(R.id.my_collection_recyclerview)
        //瀑布流
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        //设置瀑布流边距
        recyclerView?.addItemDecoration(MyItemDecoration(context, 6, 3))
        recyclerView?.layoutManager = layoutManager
        adapter = GuidelineAdapter()
        context?.let { adapter?.setContext(it) }
        recyclerView?.adapter = adapter
        adapter?.onItemClick = {
            myCollectionViewModel?.setItem(it)
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, MyCollectionDetailFragment())
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        myCollectionViewModel?.getList()?.observe(this,{
            progressBar?.visibility = View.INVISIBLE
            swip?.isRefreshing = false
            adapter?.setList(it)
            adapter?.notifyDataSetChanged()
        })
    }

    private fun setBack() {
        tv_back?.visibility = View.VISIBLE
        tv_back?.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun setTitle(mtitle: String?) {
        tv_title?.visibility = View.VISIBLE
        tv_title?.text = mtitle
    }
}