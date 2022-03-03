package com.example.travelapp.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.travelapp.R
import com.example.travelapp.fragment.MyRecognitionDetailFragment
import com.example.travelapp.fragment.MyRecognitionFragment
import com.example.travelapp.viewmodel.MyRecognitionViewModel

class MyRecognitionActivity : AppCompatActivity() {
    private var myRecognitionViewModel: MyRecognitionViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_recognition)
        myRecognitionViewModel = ViewModelProvider(this).get(MyRecognitionViewModel::class.java)
        showFragment(MyRecognitionFragment.newInstance())
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        supportFragmentManager.fragments.last()?.let{
            if(it is MyRecognitionDetailFragment){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MyRecognitionFragment())
                    .commit()
            }else{
                finish()
            }
        }
    }
}