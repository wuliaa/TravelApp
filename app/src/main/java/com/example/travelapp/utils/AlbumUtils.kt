package com.example.travelapp.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AlbumUtils {
    companion object{
        fun openAlbum(activity:Activity):Intent {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }else{
                val intent = Intent("android.intent.action.GET_CONTENT")
                intent.type = "image/*"
                return intent
            }
            return Intent()
        }

        fun getImagePath(uri: Uri?, selection: String?,contentResolver:ContentResolver): String? {
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
        //4.4后 判断封装情况
        @TargetApi(19)
        fun handleImageOnKitKat(data: Intent,context: Context):String? {
            val uri = data.data
            var imagePath:String?=null
            Log.d("TAG", "handleImageOnKitKat: uri is $uri")
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // 如果是document类型的Uri，则通过document id处理
                val docId = DocumentsContract.getDocumentId(uri)
                if ("com.android.providers.media.documents" == uri!!.authority) {
                    val id = docId.split(":".toRegex()).toTypedArray()[1] // 解析出数字格式的id
                    val selection = MediaStore.Images.Media._ID + "=" + id
                    imagePath = getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection,context.contentResolver)
                } else if ("com.android.providers.downloads.documents" == uri.authority) {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(docId)
                    )
                    imagePath = getImagePath(contentUri, null,context.contentResolver)
                }
            } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
                // 如果是content类型的Uri，则使用普通方式处理
                imagePath = getImagePath(uri, null,context.contentResolver)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                // 如果是file类型的Uri，直接获取图片路径即可
                imagePath = uri.path
            }
            return imagePath
        }

        fun handleImageBeforeKitKat(data: Intent,contentResolver:ContentResolver) :String?{
            val uri = data.data
            return getImagePath(uri, null,contentResolver)
        }
    }
}