package com.example.travelapp.bean;

import java.util.Date;

public class GuidelineVo {
    private Long id;

    private String photo;

    private String portrait;

    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public GuidelineVo(){

    }
    public GuidelineVo(Long id, String photo, String portrait, String nickname, String landmarkName, String title, String content, Date createTime) {
        this.id = id;
        this.photo = photo;
        this.portrait = portrait;
        this.nickname = nickname;
        this.landmarkName = landmarkName;
        this.title = title;
        this.content = content;
        this.createTime = createTime;
    }

    private String nickname;

    private String landmarkName;

    private String title;

    private String content;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLandmarkName() {
        return landmarkName;
    }

    public void setLandmarkName(String landmarkName) {
        this.landmarkName = landmarkName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
