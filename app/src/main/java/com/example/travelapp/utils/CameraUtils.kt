package com.example.travelapp.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

class CameraUtils {
    companion object {
        fun camera(activity: Activity, externalCacheDir: File?): Uri? {
            if (ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //去请求相机权限
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 2)
                return null
            } else {
                val outputImage = File(externalCacheDir, "output_image.jpg")
                try {
                    if (outputImage.exists()) {
                        outputImage.delete()
                    }
                    outputImage.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return if (Build.VERSION.SDK_INT < 24) {
                    Uri.fromFile(outputImage)
                } else {
                    FileProvider.getUriForFile(
                        activity,
                        "com.example.travelapp.fileprovider",//定义唯一标识，关联后面的内容提供器
                        outputImage
                    )
                }
            }
        }
    }


}