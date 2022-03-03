package com.example.travelapp.activity

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.travelapp.MainActivity
import com.example.travelapp.R
import com.example.travelapp.base.BaseActivity
import com.example.travelapp.utils.NetUtils
import com.example.travelapp.utils.UserUtils
import com.example.travelapp.utils.XToastUtils
import com.example.travelapp.viewmodel.RegisterViewModel
import com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet
import com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet.BottomListSheetBuilder
import com.xuexiang.xutil.app.ActivityUtils
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.et_pass_word
import kotlinx.android.synthetic.main.activity_register.et_user_name
import kotlinx.android.synthetic.main.activity_register.progressbar
import java.io.File
import java.io.IOException


class RegisterActivity : BaseActivity() {
    private var registerViewModel: RegisterViewModel? = null
    private var imagePath:String?=null
    private var file:File?=null
    companion object {
        //控制两种打开方式
        val TAKE_PHOTO = 1
        val CHOOSE_PHOTO = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setBack()
        setTitle("注册")
        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        initView()
        takePhoto()
        getRegisterResult()
    }

    private fun initView() {
        btn_register.isEnabled = false
        progressbar.visibility = View.INVISIBLE
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val t1 = et_user_name.text.toString().isNotEmpty()
                val t2 = et_pass_word.text.toString().isNotEmpty()
                btn_register.isEnabled = t1 and t2
            }

            override fun afterTextChanged(s: Editable) {}
        }
        et_user_name.addTextChangedListener(textWatcher)
        et_pass_word.addTextChangedListener(textWatcher)
        btn_register.setOnClickListener {
            if (!NetUtils.isConnected(applicationContext)) {
                XToastUtils.error("无网络连接")
            } else if(file == null){
                XToastUtils.error("未选择头像")
            }else{
                registerViewModel?.register(
                    et_user_name.text.toString(),
                    et_pass_word.text.toString(),
                    file!!
                )
                progressbar.visibility = View.VISIBLE
            }
        }
    }

    private fun getRegisterResult(){
        registerViewModel?.getResult()?.observe(this, {
            progressbar.visibility = View.INVISIBLE
            if (it.getString("code") != "200") {
                XToastUtils.error(it.getString("message"))
            } else {
                XToastUtils.success("注册成功")
                spOperation(it.getString("body"))
                val intentToFinish = Intent(this, MainActivity::class.java)
                intentToFinish.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intentToFinish)
                finish()
            }
        })
    }

    private fun spOperation(userId:String){
        UserUtils.username = et_user_name.text.toString()
        UserUtils.password = et_pass_word.text.toString()
        UserUtils.userid = userId
    }

    private fun takePhoto() {
        user_head.setOnClickListener {
            BottomListSheetBuilder(this)
                .addItem("拍照")
                .addItem("上传图片")
                .addItem("取消")
                .setIsCenter(true)
                .setOnSheetItemClickListener { dialog: BottomSheet, itemView: View?, position: Int, tag: String? ->
                    dialog.dismiss()
                    when (position) {
                        0 -> camera()
                        1 -> openAlbum()
                    }
                }
                .build()
                .show()
        }
    }

    var imageUri: Uri? = null

    private fun camera() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //去请求相机权限
            requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 2)
        }else{
            val outputImage = File(externalCacheDir, "output_image.jpg")
            try {
                if (outputImage.exists()) {
                    outputImage.delete()
                }
                outputImage.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            imageUri = if (Build.VERSION.SDK_INT < 24) {
                Uri.fromFile(outputImage)
            } else {
                FileProvider.getUriForFile(
                    this,
                    "com.example.travelapp.fileprovider",//定义唯一标识，关联后面的内容提供器
                    outputImage
                )
            }
            // 3. 启动相机程序
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, TAKE_PHOTO)
        }
    }

    private fun openAlbum() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }else{
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            startActivityForResult(intent, CHOOSE_PHOTO) // 打开相册
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAlbum()
            } else {
                Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TAKE_PHOTO -> if (resultCode == RESULT_OK) {
                file = File(externalCacheDir, "output_image.jpg")
                try {
                    // 将拍摄的照片显示出来
                    val bitmap =
                        BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))
                    Glide.with(this).load(bitmap).into(user_head)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            CHOOSE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                // 判断手机系统版本号
                if (Build.VERSION.SDK_INT >= 19) {
                    // 4.4及以上系统使用这个方法处理图片
                    if (data != null) {
                        handleImageOnKitKat(data)
                    }
                } else {
                    // 4.4以下系统使用这个方法处理图片
                    if (data != null) {
                        handleImageBeforeKitKat(data)
                    }
                }
                file = File(imagePath)
            }
        }
    }

    private fun getImagePath(uri: Uri?, selection: String?): String? {
        var path: String? = null
        // 通过Uri和selection来获取真实的图片路径
        val cursor = contentResolver.query(uri!!, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }

    private fun displayImage(imagePath: String?) {
        if (imagePath != null) {
            Glide.with(this).load(imagePath).into(user_head)
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show()
        }
    }

    //4.4后 判断封装情况
    @TargetApi(19)
    private fun handleImageOnKitKat(data: Intent) {
        val uri = data.data
        Log.d("TAG", "handleImageOnKitKat: uri is $uri")
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri!!.authority) {
                val id = docId.split(":".toRegex()).toTypedArray()[1] // 解析出数字格式的id
                val selection = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(docId)
                )
                imagePath = getImagePath(contentUri, null)
            }
        } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.path
        }
        displayImage(imagePath) // 根据图片路径显示图片
    }

    private fun handleImageBeforeKitKat(data: Intent) {
        val uri = data.data
        imagePath = getImagePath(uri, null)
        displayImage(imagePath)
    }

}