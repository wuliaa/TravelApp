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
import com.example.travelapp.LiveDataBus
import com.example.travelapp.R
import com.example.travelapp.activity.ChatActivity
import com.example.travelapp.adapter.MyDialogAdapter
import com.example.travelapp.base.BaseFragment
import com.example.travelapp.bean.DialogVo
import com.example.travelapp.viewmodel.MyDialogViewModel
import com.xuexiang.xutil.app.ActivityUtils
import kotlinx.android.synthetic.main.layout_toolbar.*

class MyDialogFragment : BaseFragment() {

    private var myDialogViewModel: MyDialogViewModel? = null
    private var recyclerView: RecyclerView? = null
    private var adapter:MyDialogAdapter?=null
    private var progressBar: ProgressBar?=null
    private var swip:SwipeRefreshLayout?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myDialogViewModel = ViewModelProvider(requireActivity()).get(MyDialogViewModel::class.java)
        initViews()
    }

    private fun initViews(){
        setBack()
        setTitle("我的对话")
        initRecyclerView()
        progressBar = requireActivity().findViewById(R.id.my_dialog_progressbar)
        progressBar?.visibility = View.VISIBLE
        swip = requireActivity().findViewById(R.id.swipe_dialog)
        swip?.setOnRefreshListener {
            myDialogViewModel?.refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        myDialogViewModel?.asyncData()
        myDialogViewModel?.getList()?.observe(viewLifecycleOwner, {
            progressBar?.visibility = View.INVISIBLE
            swip?.isRefreshing = false
            adapter?.setList(it)
            adapter?.notifyDataSetChanged()
        })
    }

    private fun initRecyclerView(){
        recyclerView = requireActivity().findViewById(R.id.my_dialog_recyclerview)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView?.layoutManager = layoutManager
        adapter = MyDialogAdapter()
        context?.let { adapter?.setContext(it) }
        recyclerView?.adapter = adapter
        adapter?.onItemClick ={
            LiveDataBus.with<DialogVo>("dialog").value = it
            ActivityUtils.startActivity(ChatActivity::class.java)
        }
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