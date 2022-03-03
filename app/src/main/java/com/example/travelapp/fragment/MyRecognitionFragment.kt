package com.example.travelapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.travelapp.R
import com.example.travelapp.adapter.MyRecognitionAdapter
import com.example.travelapp.base.BaseFragment
import com.example.travelapp.viewmodel.MyRecognitionViewModel
import kotlinx.android.synthetic.main.fragment_my_recognition.*
import kotlinx.android.synthetic.main.layout_toolbar.*


class MyRecognitionFragment : BaseFragment() {
    private var myRecognitionViewModel: MyRecognitionViewModel? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: MyRecognitionAdapter? = null
    private var progressBar:ProgressBar?=null
    private var swip: SwipeRefreshLayout?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_recognition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myRecognitionViewModel =
            ViewModelProvider(requireActivity()).get(MyRecognitionViewModel::class.java)
        initViews()
    }

    private fun initViews() {
        setBack()
        setTitle("我的地标识别")
        initRecyclerView()
        progressBar = requireActivity().findViewById(R.id.my_recognition_progressbar)
        progressBar?.visibility = View.VISIBLE
        swip = requireActivity().findViewById(R.id.swipe_recognition)
        swip?.setOnRefreshListener {
            myRecognitionViewModel?.refresh()
        }
    }

    private fun initRecyclerView() {
        myRecognitionViewModel?.asyncData()
        recyclerView = requireActivity().findViewById(R.id.my_recognition_recyclerview)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView?.layoutManager = layoutManager
        adapter = MyRecognitionAdapter()
        recyclerView?.adapter = adapter
        myRecognitionViewModel?.getList()?.observe(viewLifecycleOwner, {
            progressBar?.visibility = View.INVISIBLE
            swip?.isRefreshing = false
            adapter?.data?.clear()
            adapter?.loadMore(it)
            adapter?.notifyDataSetChanged()
        })
        adapter?.setOnItemClickListener { itemView, item, position ->
            myRecognitionViewModel?.setItem(item)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, MyRecognitionDetailFragment())
                .commit()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MyRecognitionFragment()
    }

    private fun setBack() {
        tv_back?.visibility = View.VISIBLE
        tv_back?.setOnClickListener { requireActivity().finish() }
    }

    private fun setTitle(mtitle: String?) {
        tv_title?.visibility = View.VISIBLE
        tv_title?.text = mtitle
    }

}