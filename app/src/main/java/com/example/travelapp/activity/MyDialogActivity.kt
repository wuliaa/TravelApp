package com.example.travelapp.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.travelapp.R
import com.example.travelapp.fragment.MyDialogFragment
import com.example.travelapp.viewmodel.MyDialogViewModel

class MyDialogActivity : AppCompatActivity() {
    private var myDialogViewModel: MyDialogViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_dialog)
        myDialogViewModel = ViewModelProvider(this).get(MyDialogViewModel::class.java)
        showFragment(MyDialogFragment())
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}