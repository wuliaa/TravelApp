package com.example.travelapp.bean;

import java.io.Serializable;

public class DataContent implements Serializable {
    private Integer action;//动作类型
    private ChatMsg chatMsg;//用户的聊天内容
    private String extend;//需要签收的消息id:id1,id2,id3

    public DataContent(Integer action, ChatMsg chatMsg) {
        this.action = action;
        this.chatMsg = chatMsg;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public ChatMsg getChatMsg() {
        return chatMsg;
    }

    public void setChatMsg(ChatMsg chatMsg) {
        this.chatMsg = chatMsg;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }
}
