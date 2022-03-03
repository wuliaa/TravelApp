package com.example.travelapp.bean

class ChatModel(
    var portrait: String?, var content: String?,//收发类型
    var type: Int
) {
    companion object{
        //发送类型
        const val SEND = 0
        //接收类型
        const val RECEIVE = 1
    }

}