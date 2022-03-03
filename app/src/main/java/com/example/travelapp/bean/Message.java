package com.example.travelapp.bean;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_table")
public class Message {
    @PrimaryKey(autoGenerate = true)
    long id;
    String content;
    String from_user;
    String to_user;
    int flag;

    public Message(String content, String from_user, String to_user, int flag) {
        this.content = content;
        this.from_user = from_user;
        this.to_user = to_user;
        this.flag = flag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFrom_user() {
        return from_user;
    }

    public void setFrom_user(String from_user) {
        this.from_user = from_user;
    }

    public String getTo_user() {
        return to_user;
    }

    public void setTo_user(String to_user) {
        this.to_user = to_user;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
